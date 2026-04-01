package com.tictactoe.multiplayer.service;

import com.tictactoe.multiplayer.model.GameRoom;
import com.tictactoe.multiplayer.model.GameState;
import com.tictactoe.multiplayer.model.Player;
import com.tictactoe.multiplayer.repository.GameRoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final GameRoomRepository roomRepository;
    private final SimpMessagingTemplate messaging;

    @Value("${app.room.max-rooms:100}")
    private int maxRooms;

    public RoomService(GameRoomRepository roomRepository, SimpMessagingTemplate messaging) {
        this.roomRepository = roomRepository;
        this.messaging      = messaging;
    }

    public Optional<GameRoom> createRoom(Player playerX) {
        if (roomRepository.count() >= maxRooms) {
            log.warn("Room limit reached");
            return Optional.empty();
        }
        String roomId = generateRoomId();
        playerX.setSymbol("X");
        playerX.setRoomId(roomId);

        GameRoom room = GameRoom.create(roomId, playerX);
        roomRepository.save(room);
        log.info("Room created: {} by {}", roomId, playerX.getName());
        return Optional.of(room);
    }

    public Optional<GameRoom> joinRoom(String roomId, Player playerO) {
        Optional<GameRoom> opt = roomRepository.findById(roomId);
        if (opt.isEmpty()) return Optional.empty();

        GameRoom room = opt.get();
        if (room.getRoomStatus() != GameRoom.RoomStatus.OPEN) return Optional.empty();

        playerO.setSymbol("O");
        playerO.setRoomId(roomId);
        room.setPlayerO(playerO);
        room.setRoomStatus(GameRoom.RoomStatus.FULL);
        room.setGameState(GameState.started(roomId, room.getPlayerX(), playerO));
        room.touch();

        roomRepository.save(room);
        log.info("Player {} joined room {}", playerO.getName(), roomId);
        broadcast(room);
        return Optional.of(room);
    }

    public void leaveRoom(String sessionId) {
        roomRepository.findBySessionId(sessionId).ifPresent(room -> {
            GameState state = room.getGameState();
            if (!state.isOver()) {
                state.setStatus(GameState.Status.ABANDONED);
                state.setMessage("Opponent disconnected");
                room.setRoomStatus(GameRoom.RoomStatus.ABANDONED);
                broadcast(room);
            }
            roomRepository.removeSession(sessionId);
            roomRepository.delete(room.getRoomId());
        });
    }

    public Optional<GameRoom> findRoom(String roomId)       { return roomRepository.findById(roomId); }
    public Optional<GameRoom> findRoomBySession(String sid) { return roomRepository.findBySessionId(sid); }
    public Collection<GameRoom> listOpenRooms()             { return roomRepository.findOpenRooms(); }
    public Collection<GameRoom> listAllRooms()              { return roomRepository.findAll(); }

    public void broadcast(GameRoom room) {
        messaging.convertAndSend("/topic/room/" + room.getRoomId(), room.getGameState());
    }

    public void broadcastToUser(String sessionId, Object payload) {
        messaging.convertAndSendToUser(sessionId, "/queue/room", payload);
    }

    private String generateRoomId() {
        String id;
        do {
            id = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (roomRepository.existsById(id));
        return id;
    }
}
