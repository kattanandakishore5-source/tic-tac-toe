package com.tictactoe.multiplayer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(unique = true, nullable = false, length = 10)
    private String playerId;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String avatarColor;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant lastOnline;
    private Instant lastPlayed;

    // Stats
    private int wins = 0;
    private int losses = 0;
    private int draws = 0;
    private int totalGames = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rank rank = Rank.BRONZE;

    public UserProfile() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAvatarColor() { return avatarColor; }
    public void setAvatarColor(String avatarColor) { this.avatarColor = avatarColor; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getLastOnline() { return lastOnline; }
    public void setLastOnline(Instant lastOnline) { this.lastOnline = lastOnline; }

    public Instant getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(Instant lastPlayed) { this.lastPlayed = lastPlayed; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getDraws() { return draws; }
    public void setDraws(int draws) { this.draws = draws; }

    public int getTotalGames() { return totalGames; }
    public void setTotalGames(int totalGames) { this.totalGames = totalGames; }

    public Rank getRank() { return rank; }
    public void setRank(Rank rank) { this.rank = rank; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private User user;
        private String playerId;
        private String displayName;
        private String avatarColor;
        private Instant createdAt = Instant.now();
        private Instant lastOnline;
        private Instant lastPlayed;
        private int wins = 0;
        private int losses = 0;
        private int draws = 0;
        private int totalGames = 0;
        private Rank rank = Rank.BRONZE;

        public Builder user(User user) { this.user = user; return this; }
        public Builder playerId(String playerId) { this.playerId = playerId; return this; }
        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        public Builder avatarColor(String avatarColor) { this.avatarColor = avatarColor; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder lastOnline(Instant lastOnline) { this.lastOnline = lastOnline; return this; }
        public Builder lastPlayed(Instant lastPlayed) { this.lastPlayed = lastPlayed; return this; }
        public Builder wins(int wins) { this.wins = wins; return this; }
        public Builder losses(int losses) { this.losses = losses; return this; }
        public Builder draws(int draws) { this.draws = draws; return this; }
        public Builder totalGames(int totalGames) { this.totalGames = totalGames; return this; }
        public Builder rank(Rank rank) { this.rank = rank; return this; }

        public UserProfile build() {
            UserProfile profile = new UserProfile();
            profile.setUser(user);
            profile.setPlayerId(playerId);
            profile.setDisplayName(displayName);
            profile.setAvatarColor(avatarColor);
            profile.setCreatedAt(createdAt);
            profile.setLastOnline(lastOnline);
            profile.setLastPlayed(lastPlayed);
            profile.setWins(wins);
            profile.setLosses(losses);
            profile.setDraws(draws);
            profile.setTotalGames(totalGames);
            profile.setRank(rank);
            return profile;
        }
    }
}
