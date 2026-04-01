package com.tictactoe.multiplayer.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "game_history")
public class GameHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_x_id")
    private UserProfile playerX;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_o_id")
    private UserProfile playerO;

    @ManyToOne
    @JoinColumn(name = "winner_id")
    private UserProfile winner; // null means draw

    @Column(nullable = false)
    private String roomId;

    @Column(nullable = false)
    private Instant playedAt = Instant.now();

    private long durationSeconds;

    public GameHistory() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserProfile getPlayerX() { return playerX; }
    public void setPlayerX(UserProfile playerX) { this.playerX = playerX; }

    public UserProfile getPlayerO() { return playerO; }
    public void setPlayerO(UserProfile playerO) { this.playerO = playerO; }

    public UserProfile getWinner() { return winner; }
    public void setWinner(UserProfile winner) { this.winner = winner; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public Instant getPlayedAt() { return playedAt; }
    public void setPlayedAt(Instant playedAt) { this.playedAt = playedAt; }

    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }
}
