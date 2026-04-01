package com.tictactoe.multiplayer.model;

import java.time.Instant;

public class GameRoom {

    public enum RoomStatus {
        OPEN, FULL, FINISHED, ABANDONED
    }

    private String roomId;
    private Player playerX;
    private Player playerO;
    private GameState gameState;
    private RoomStatus roomStatus = RoomStatus.OPEN;
    private Instant createdAt    = Instant.now();
    private Instant lastActivity = Instant.now();

    public GameRoom() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String roomId;
        private Player playerX;
        private Player playerO;
        private GameState gameState;
        private RoomStatus roomStatus = RoomStatus.OPEN;
        private Instant createdAt    = Instant.now();
        private Instant lastActivity = Instant.now();

        public Builder roomId(String v)         { this.roomId = v; return this; }
        public Builder playerX(Player v)        { this.playerX = v; return this; }
        public Builder playerO(Player v)        { this.playerO = v; return this; }
        public Builder gameState(GameState v)   { this.gameState = v; return this; }
        public Builder roomStatus(RoomStatus v) { this.roomStatus = v; return this; }
        public Builder createdAt(Instant v)     { this.createdAt = v; return this; }
        public Builder lastActivity(Instant v)  { this.lastActivity = v; return this; }

        public GameRoom build() {
            GameRoom r     = new GameRoom();
            r.roomId       = roomId;
            r.playerX      = playerX;
            r.playerO      = playerO;
            r.gameState    = gameState;
            r.roomStatus   = roomStatus;
            r.createdAt    = createdAt;
            r.lastActivity = lastActivity;
            return r;
        }
    }

    public static GameRoom create(String roomId, Player playerX) {
        GameRoom room = GameRoom.builder()
                .roomId(roomId)
                .playerX(playerX)
                .gameState(GameState.waiting(roomId, playerX))
                .build();
        playerX.setRoomId(roomId);
        return room;
    }

    public boolean isFull() {
        return playerX != null && playerO != null;
    }

    public boolean hasPlayer(String sessionId) {
        return (playerX != null && playerX.getSessionId().equals(sessionId))
            || (playerO != null && playerO.getSessionId().equals(sessionId));
    }

    public Player getPlayer(String sessionId) {
        if (playerX != null && playerX.getSessionId().equals(sessionId)) return playerX;
        if (playerO != null && playerO.getSessionId().equals(sessionId)) return playerO;
        return null;
    }

    public void touch() { this.lastActivity = Instant.now(); }

    // Getters and Setters

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public Player getPlayerX() { return playerX; }
    public void setPlayerX(Player playerX) { this.playerX = playerX; }

    public Player getPlayerO() { return playerO; }
    public void setPlayerO(Player playerO) { this.playerO = playerO; }

    public GameState getGameState() { return gameState; }
    public void setGameState(GameState gameState) { this.gameState = gameState; }

    public RoomStatus getRoomStatus() { return roomStatus; }
    public void setRoomStatus(RoomStatus roomStatus) { this.roomStatus = roomStatus; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getLastActivity() { return lastActivity; }
    public void setLastActivity(Instant lastActivity) { this.lastActivity = lastActivity; }
}
