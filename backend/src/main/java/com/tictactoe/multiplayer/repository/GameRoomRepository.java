package com.tictactoe.multiplayer.repository;

import com.tictactoe.multiplayer.model.GameRoom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class GameRoomRepository {

    private static final Logger log = LoggerFactory.getLogger(GameRoomRepository.class);

    private final Map<String, GameRoom> rooms            = new ConcurrentHashMap<>();
    private final Map<String, String>   sessionRoomIndex = new ConcurrentHashMap<>();

    public GameRoom save(GameRoom room) {
        rooms.put(room.getRoomId(), room);
        indexSession(room);
        return room;
    }

    public Optional<GameRoom> findById(String roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    public Optional<GameRoom> findBySessionId(String sessionId) {
        String roomId = sessionRoomIndex.get(sessionId);
        if (roomId == null) return Optional.empty();
        return findById(roomId);
    }

    public Collection<GameRoom> findAll() {
        return rooms.values();
    }

    public Collection<GameRoom> findOpenRooms() {
        return rooms.values().stream()
                .filter(r -> r.getRoomStatus() == GameRoom.RoomStatus.OPEN)
                .toList();
    }

    public void delete(String roomId) {
        GameRoom room = rooms.remove(roomId);
        if (room != null) deindexRoom(room);
    }

    public boolean existsById(String roomId) {
        return rooms.containsKey(roomId);
    }

    public int count() {
        return rooms.size();
    }

    public void indexSession(String sessionId, String roomId) {
        sessionRoomIndex.put(sessionId, roomId);
    }

    public void removeSession(String sessionId) {
        sessionRoomIndex.remove(sessionId);
    }

    public int pruneIdleRooms(long timeoutMinutes) {
        Instant cutoff = Instant.now().minusSeconds(timeoutMinutes * 60);
        long removed = rooms.values().stream()
                .filter(r -> r.getLastActivity().isBefore(cutoff))
                .peek(r -> delete(r.getRoomId()))
                .count();
        if (removed > 0) log.info("Pruned {} idle room(s)", removed);
        return (int) removed;
    }

    private void indexSession(GameRoom room) {
        if (room.getPlayerX() != null)
            sessionRoomIndex.put(room.getPlayerX().getSessionId(), room.getRoomId());
        if (room.getPlayerO() != null)
            sessionRoomIndex.put(room.getPlayerO().getSessionId(), room.getRoomId());
    }

    private void deindexRoom(GameRoom room) {
        if (room.getPlayerX() != null) sessionRoomIndex.remove(room.getPlayerX().getSessionId());
        if (room.getPlayerO() != null) sessionRoomIndex.remove(room.getPlayerO().getSessionId());
    }
}
