package com.tictactoe.multiplayer.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "friendships", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sender_id", "receiver_id"})
})
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id")
    private UserProfile sender;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_id")
    private UserProfile receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Friendship() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserProfile getSender() { return sender; }
    public void setSender(UserProfile sender) { this.sender = sender; }

    public UserProfile getReceiver() { return receiver; }
    public void setReceiver(UserProfile receiver) { this.receiver = receiver; }

    public FriendshipStatus getStatus() { return status; }
    public void setStatus(FriendshipStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
