package com.tictactoe.multiplayer.model;

import java.time.Instant;
import java.util.Arrays;

public class GameState {

    public enum Status {
        WAITING, IN_PROGRESS, X_WON, O_WON, DRAW, ABANDONED
    }

    public static final int[][] WIN_COMBOS = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
        {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
        {0, 4, 8}, {2, 4, 6}
    };

    private String roomId;
    private String[] board;
    private String currentTurn;
    private Status status;
    private int[] winningLine;
    private PlayerInfo playerX;
    private PlayerInfo playerO;
    private Instant timestamp = Instant.now();
    private String message;

    public GameState() {}

    // ── Nested PlayerInfo ─────────────────────────────────────────────

    public static class PlayerInfo {
        private String name;
        private String symbol;
        private String sessionId;

        public PlayerInfo() {}

        public PlayerInfo(String name, String symbol, String sessionId) {
            this.name = name;
            this.symbol = symbol;
            this.sessionId = sessionId;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }

    // ── Builder ───────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String roomId;
        private String[] board;
        private String currentTurn;
        private Status status;
        private int[] winningLine;
        private PlayerInfo playerX;
        private PlayerInfo playerO;
        private Instant timestamp = Instant.now();
        private String message;

        public Builder roomId(String v)       { this.roomId = v; return this; }
        public Builder board(String[] v)      { this.board = v; return this; }
        public Builder currentTurn(String v)  { this.currentTurn = v; return this; }
        public Builder status(Status v)       { this.status = v; return this; }
        public Builder winningLine(int[] v)   { this.winningLine = v; return this; }
        public Builder playerX(PlayerInfo v)  { this.playerX = v; return this; }
        public Builder playerO(PlayerInfo v)  { this.playerO = v; return this; }
        public Builder timestamp(Instant v)   { this.timestamp = v; return this; }
        public Builder message(String v)      { this.message = v; return this; }

        public GameState build() {
            GameState s = new GameState();
            s.roomId      = roomId;
            s.board       = board;
            s.currentTurn = currentTurn;
            s.status      = status;
            s.winningLine = winningLine;
            s.playerX     = playerX;
            s.playerO     = playerO;
            s.timestamp   = timestamp;
            s.message     = message;
            return s;
        }
    }

    // ── Factory methods ───────────────────────────────────────────────

    public static GameState waiting(String roomId, Player playerX) {
        return GameState.builder()
                .roomId(roomId)
                .board(new String[9])
                .currentTurn("X")
                .status(Status.WAITING)
                .playerX(toInfo(playerX))
                .message("Waiting for second player...")
                .build();
    }

    public static GameState started(String roomId, Player playerX, Player playerO) {
        return GameState.builder()
                .roomId(roomId)
                .board(new String[9])
                .currentTurn("X")
                .status(Status.IN_PROGRESS)
                .playerX(toInfo(playerX))
                .playerO(toInfo(playerO))
                .message("Game started - X goes first")
                .build();
    }

    private static PlayerInfo toInfo(Player p) {
        if (p == null) return null;
        return new PlayerInfo(p.getName(), p.getSymbol(), p.getSessionId());
    }

    // ── Game logic ────────────────────────────────────────────────────

    public boolean applyMove(Move move) {
        if (status != Status.IN_PROGRESS) return false;
        if (!move.getSymbol().equals(currentTurn)) return false;
        if (move.getPosition() < 0 || move.getPosition() > 8) return false;
        if (board[move.getPosition()] != null) return false;

        board[move.getPosition()] = move.getSymbol();

        for (int[] combo : WIN_COMBOS) {
            String a = board[combo[0]], b = board[combo[1]], c = board[combo[2]];
            if (a != null && a.equals(b) && a.equals(c)) {
                winningLine = combo;
                status  = a.equals("X") ? Status.X_WON : Status.O_WON;
                message = a + " wins!";
                return true;
            }
        }

        if (Arrays.stream(board).allMatch(cell -> cell != null)) {
            status  = Status.DRAW;
            message = "It's a draw!";
            return true;
        }

        currentTurn = currentTurn.equals("X") ? "O" : "X";
        message     = currentTurn + "'s turn";
        return true;
    }

    public void reset() {
        board       = new String[9];
        currentTurn = "X";
        status      = Status.IN_PROGRESS;
        winningLine = null;
        message     = "Rematch started - X goes first";
        timestamp   = Instant.now();
    }

    public boolean isOver() {
        return status == Status.X_WON || status == Status.O_WON
            || status == Status.DRAW  || status == Status.ABANDONED;
    }

    // ── Getters and Setters ───────────────────────────────────────────

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String[] getBoard() { return board; }
    public void setBoard(String[] board) { this.board = board; }

    public String getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(String currentTurn) { this.currentTurn = currentTurn; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int[] getWinningLine() { return winningLine; }
    public void setWinningLine(int[] winningLine) { this.winningLine = winningLine; }

    public PlayerInfo getPlayerX() { return playerX; }
    public void setPlayerX(PlayerInfo playerX) { this.playerX = playerX; }

    public PlayerInfo getPlayerO() { return playerO; }
    public void setPlayerO(PlayerInfo playerO) { this.playerO = playerO; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
