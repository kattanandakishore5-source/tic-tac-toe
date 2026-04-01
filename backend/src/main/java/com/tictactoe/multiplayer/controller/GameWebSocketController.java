package com.tictactoe.multiplayer.controller;

import com.tictactoe.multiplayer.model.GameRoom;
import com.tictactoe.multiplayer.model.GameState;
import com.tictactoe.multiplayer.model.Move;
import com.tictactoe.multiplayer.model.Player;
import com.tictactoe.multiplayer.service.GameService;
import com.tictactoe.multiplayer.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.tictactoe.multiplayer.repository.UserProfileRepository;

@Controller
public class GameWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(GameWebSocketController.class);

    private final RoomService roomService;
    private final GameService gameService;
    private final UserProfileRepository userProfileRepository;

    public GameWebSocketController(RoomService roomService, GameService gameService, UserProfileRepository userProfileRepository) {
        this.roomService = roomService;
        this.gameService = gameService;
        this.userProfileRepository = userProfileRepository;
    }

    private void updateLastOnline(String playerId) {
        if (playerId != null) {
            userProfileRepository.findByPlayerId(playerId).ifPresent(profile -> {
                profile.setLastOnline(java.time.Instant.now());
                userProfileRepository.save(profile);
            });
        }
    }

    @MessageMapping("/room/create")
    @SendToUser("/queue/room")
    public Object createRoom(@Payload Map<String, String> payload,
                             SimpMessageHeaderAccessor header) {
        String sessionId = header.getSessionId();
        String name      = payload.getOrDefault("name", "Player");
        String playerId  = payload.get("playerId");

        updateLastOnline(playerId);

        Player player = Player.builder().sessionId(sessionId).name(name).playerId(playerId).build();
        Optional<GameRoom> room = roomService.createRoom(player);
        if (room.isEmpty()) return Map.of("error", "Server is full");

        log.info("Room created: {} for session {}", room.get().getRoomId(), sessionId);
        return room.get().getGameState();
    }

    @MessageMapping("/room/join")
    @SendToUser("/queue/room")
    public Object joinRoom(@Payload Map<String, String> payload,
                           SimpMessageHeaderAccessor header) {
        String sessionId = header.getSessionId();
        String name      = payload.getOrDefault("name", "Player");
        String playerId  = payload.get("playerId");
        String roomId    = payload.get("roomId");

        if (roomId == null || roomId.isBlank()) {
            return Map.of("error", "roomId is required");
        }

        updateLastOnline(playerId);

        Player player = Player.builder().sessionId(sessionId).name(name).playerId(playerId).build();
        Optional<GameRoom> room = roomService.joinRoom(roomId.toUpperCase(), player);
        if (room.isEmpty()) {
            return Map.of("error", "Room " + roomId + " not found or already full");
        }

        log.info("Session {} joined room {}", sessionId, roomId);
        return room.get().getGameState();
    }

    @MessageMapping("/game/move")
    public void makeMove(@Payload Move move, SimpMessageHeaderAccessor header) {
        move.setPlayerSessionId(header.getSessionId());
        gameService.processMove(move).ifPresentOrElse(
            s  -> log.debug("Move OK: room={}", move.getRoomId()),
            () -> roomService.broadcastToUser(
                    move.getPlayerSessionId(),
                    Map.of("error", "Invalid move at position " + move.getPosition())
                  )
        );
    }

    @MessageMapping("/game/rematch")
    public void requestRematch(@Payload Map<String, String> payload,
                               SimpMessageHeaderAccessor header) {
        String roomId    = payload.get("roomId");
        String sessionId = header.getSessionId();
        gameService.requestRematch(roomId, sessionId).ifPresentOrElse(
            s  -> log.info("Rematch: room={}", roomId),
            () -> roomService.broadcastToUser(
                    sessionId,
                    Map.of("error", "Cannot rematch room " + roomId)
                  )
        );
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        log.info("Session disconnected: {}", event.getSessionId());
        roomService.leaveRoom(event.getSessionId());
    }
}


@RestController
@RequestMapping("/api")
class GameRestController {

    private static final Logger log = LoggerFactory.getLogger(GameRestController.class);

    private final RoomService roomService;
    private final GameService gameService;

    public GameRestController(RoomService roomService, GameService gameService) {
        this.roomService = roomService;
        this.gameService = gameService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "app", "tictactoe-server"));
    }

    @GetMapping("/rooms")
    public ResponseEntity<Collection<GameRoom>> listRooms() {
        return ResponseEntity.ok(roomService.listAllRooms());
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<?> getRoom(@PathVariable String roomId) {
        return roomService.findRoom(roomId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rooms/{roomId}/state")
    public ResponseEntity<?> getState(@PathVariable String roomId) {
        return gameService.getState(roomId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
