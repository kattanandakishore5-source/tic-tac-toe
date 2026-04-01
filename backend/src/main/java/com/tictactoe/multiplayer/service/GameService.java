package com.tictactoe.multiplayer.service;

import com.tictactoe.multiplayer.model.GameRoom;
import com.tictactoe.multiplayer.model.GameState;
import com.tictactoe.multiplayer.model.Move;
import com.tictactoe.multiplayer.model.Player;
import com.tictactoe.multiplayer.repository.GameRoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

import com.tictactoe.multiplayer.repository.UserProfileRepository;
import com.tictactoe.multiplayer.repository.GameHistoryRepository;
import com.tictactoe.multiplayer.model.GameHistory;
import com.tictactoe.multiplayer.model.UserProfile;
import com.tictactoe.multiplayer.model.Rank;
import java.time.Instant;

@Service
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final GameRoomRepository roomRepository;
    private final RoomService        roomService;
    private final UserProfileRepository userProfileRepository;
    private final GameHistoryRepository gameHistoryRepository;

    public GameService(GameRoomRepository roomRepository, RoomService roomService,
                       UserProfileRepository userProfileRepository,
                       GameHistoryRepository gameHistoryRepository) {
        this.roomRepository = roomRepository;
        this.roomService    = roomService;
        this.userProfileRepository = userProfileRepository;
        this.gameHistoryRepository = gameHistoryRepository;
    }

    public Optional<GameState> processMove(Move move) {
        Optional<GameRoom> opt = roomRepository.findById(move.getRoomId());
        if (opt.isEmpty()) return Optional.empty();

        GameRoom room   = opt.get();
        Player   player = room.getPlayer(move.getPlayerSessionId());
        if (player == null) return Optional.empty();
        if (!player.getSymbol().equals(room.getGameState().getCurrentTurn())) return Optional.empty();

        boolean accepted = room.getGameState().applyMove(move);
        if (!accepted) return Optional.empty();

        if (room.getGameState().isOver()) {
            room.setRoomStatus(GameRoom.RoomStatus.FINISHED);
            recordGameOutcome(room);
        }

        room.touch();
        roomRepository.save(room);
        log.info("Move: room={} pos={} symbol={}", move.getRoomId(), move.getPosition(), move.getSymbol());
        roomService.broadcast(room);
        return Optional.of(room.getGameState());
    }

    private void recordGameOutcome(GameRoom room) {
        GameState state = room.getGameState();
        Player px = room.getPlayerX();
        Player po = room.getPlayerO();

        if (px == null || po == null || px.getPlayerId() == null || po.getPlayerId() == null) {
            return;
        }

        Optional<UserProfile> userXOpt = userProfileRepository.findByPlayerId(px.getPlayerId());
        Optional<UserProfile> userOOpt = userProfileRepository.findByPlayerId(po.getPlayerId());

        if (userXOpt.isEmpty() || userOOpt.isEmpty()) return;

        UserProfile userX = userXOpt.get();
        UserProfile userO = userOOpt.get();

        long duration = java.time.Duration.between(state.getTimestamp(), Instant.now()).getSeconds();

        GameHistory history = new GameHistory();
        history.setPlayerX(userX);
        history.setPlayerO(userO);
        history.setRoomId(room.getRoomId());
        history.setPlayedAt(Instant.now());
        history.setDurationSeconds(duration);

        if (state.getStatus() == GameState.Status.X_WON) {
            history.setWinner(userX);
            updateStats(userX, true, false);
            updateStats(userO, false, false);
        } else if (state.getStatus() == GameState.Status.O_WON) {
            history.setWinner(userO);
            updateStats(userX, false, false);
            updateStats(userO, true, false);
        } else if (state.getStatus() == GameState.Status.DRAW) {
            updateStats(userX, false, true);
            updateStats(userO, false, true);
        }

        gameHistoryRepository.save(history);
    }

    private void updateStats(UserProfile profile, boolean won, boolean draw) {
        profile.setTotalGames(profile.getTotalGames() + 1);
        profile.setLastPlayed(Instant.now());
        if (won) profile.setWins(profile.getWins() + 1);
        else if (draw) profile.setDraws(profile.getDraws() + 1);
        else profile.setLosses(profile.getLosses() + 1);

        int wins = profile.getWins();
        if (wins >= 50) profile.setRank(Rank.DIAMOND);
        else if (wins >= 30) profile.setRank(Rank.PLATINUM);
        else if (wins >= 15) profile.setRank(Rank.GOLD);
        else if (wins >= 5) profile.setRank(Rank.SILVER);
        else profile.setRank(Rank.BRONZE);

        userProfileRepository.save(profile);
    }

    public Optional<GameState> requestRematch(String roomId, String sessionId) {
        Optional<GameRoom> opt = roomRepository.findById(roomId);
        if (opt.isEmpty()) return Optional.empty();

        GameRoom room = opt.get();
        if (!room.hasPlayer(sessionId))    return Optional.empty();
        if (!room.getGameState().isOver()) return Optional.empty();

        room.getGameState().reset();
        room.setRoomStatus(GameRoom.RoomStatus.FULL);
        room.touch();
        roomRepository.save(room);
        log.info("Rematch started: room={}", roomId);
        roomService.broadcast(room);
        return Optional.of(room.getGameState());
    }

    public Optional<GameState> getState(String roomId) {
        return roomRepository.findById(roomId).map(GameRoom::getGameState);
    }
}
