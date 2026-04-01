package com.tictactoe.multiplayer;

import com.tictactoe.multiplayer.model.GameState;
import com.tictactoe.multiplayer.model.Move;
import com.tictactoe.multiplayer.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    private GameState state;

    @BeforeEach
    void setUp() {
        Player x = Player.builder().sessionId("sx").name("Alice").symbol("X").build();
        Player o = Player.builder().sessionId("so").name("Bob").symbol("O").build();
        state = GameState.started("ROOM01", x, o);
    }

    @Test
    void validMove() {
        assertTrue(move(0, "X"));
        assertEquals("X", state.getBoard()[0]);
    }

    @Test
    void occupiedCell() {
        move(4, "X");
        assertFalse(move(4, "O"));
    }

    @Test
    void xWins() {
        move(0, "X"); move(3, "O");
        move(1, "X"); move(4, "O");
        move(2, "X");
        assertEquals(GameState.Status.X_WON, state.getStatus());
    }

    @Test
    void draw() {
        int[] pos    = {0, 1, 2, 4, 3, 5, 7, 6, 8};
        String[] sym = {"X","O","X","O","X","O","X","O","X"};
        for (int i = 0; i < 9; i++) move(pos[i], sym[i]);
        assertEquals(GameState.Status.DRAW, state.getStatus());
    }

    @Test
    void rematch() {
        move(0, "X");
        state.setStatus(GameState.Status.X_WON);
        state.reset();
        assertEquals(GameState.Status.IN_PROGRESS, state.getStatus());
        assertNull(state.getBoard()[0]);
    }

    private boolean move(int pos, String symbol) {
        return state.applyMove(Move.builder()
                .roomId("ROOM01")
                .playerSessionId(symbol.equals("X") ? "sx" : "so")
                .position(pos)
                .symbol(symbol)
                .build());
    }
}
