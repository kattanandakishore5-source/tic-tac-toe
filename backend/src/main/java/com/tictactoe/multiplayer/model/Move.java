package com.tictactoe.multiplayer.model;

public class Move {

    private String roomId;
    private String playerSessionId;
    private int position;
    private String symbol;

    public Move() {}

    public Move(String roomId, String playerSessionId, int position, String symbol) {
        this.roomId = roomId;
        this.playerSessionId = playerSessionId;
        this.position = position;
        this.symbol = symbol;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String roomId;
        private String playerSessionId;
        private int position;
        private String symbol;

        public Builder roomId(String v)              { this.roomId = v; return this; }
        public Builder playerSessionId(String v)     { this.playerSessionId = v; return this; }
        public Builder position(int v)               { this.position = v; return this; }
        public Builder symbol(String v)              { this.symbol = v; return this; }

        public Move build() {
            return new Move(roomId, playerSessionId, position, symbol);
        }
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getPlayerSessionId() { return playerSessionId; }
    public void setPlayerSessionId(String playerSessionId) { this.playerSessionId = playerSessionId; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
}
