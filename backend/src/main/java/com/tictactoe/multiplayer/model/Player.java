package com.tictactoe.multiplayer.model;

import java.time.Instant;

public class Player {

    private String sessionId;
    private String name;
    private String playerId;
    private String symbol;
    private String roomId;
    private boolean ready;
    private Instant joinedAt = Instant.now();

    public Player() {}

    public Player(String sessionId, String name, String playerId, String symbol,
                  String roomId, boolean ready, Instant joinedAt) {
        this.sessionId = sessionId;
        this.name = name;
        this.playerId = playerId;
        this.symbol = symbol;
        this.roomId = roomId;
        this.ready = ready;
        this.joinedAt = joinedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String sessionId;
        private String name;
        private String playerId;
        private String symbol;
        private String roomId;
        private boolean ready;
        private Instant joinedAt = Instant.now();

        public Builder sessionId(String v)  { this.sessionId = v; return this; }
        public Builder name(String v)       { this.name = v; return this; }
        public Builder playerId(String v)   { this.playerId = v; return this; }
        public Builder symbol(String v)     { this.symbol = v; return this; }
        public Builder roomId(String v)     { this.roomId = v; return this; }
        public Builder ready(boolean v)     { this.ready = v; return this; }
        public Builder joinedAt(Instant v)  { this.joinedAt = v; return this; }

        public Player build() {
            return new Player(sessionId, name, playerId, symbol, roomId, ready, joinedAt);
        }
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public boolean isReady() { return ready; }
    public void setReady(boolean ready) { this.ready = ready; }

    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }

    public boolean isX() { return "X".equals(symbol); }
    public boolean isO() { return "O".equals(symbol); }
}
