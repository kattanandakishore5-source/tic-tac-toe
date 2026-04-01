package com.tictactoe.multiplayer.repository;

import com.tictactoe.multiplayer.model.Friendship;
import com.tictactoe.multiplayer.model.FriendshipStatus;
import com.tictactoe.multiplayer.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT f FROM Friendship f WHERE (f.sender = :user OR f.receiver = :user) AND f.status = :status")
    List<Friendship> findByUserAndStatus(UserProfile user, FriendshipStatus status);

    List<Friendship> findByReceiverAndStatus(UserProfile receiver, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE (f.sender = :user1 AND f.receiver = :user2) OR (f.sender = :user2 AND f.receiver = :user1)")
    Optional<Friendship> findByUsers(UserProfile user1, UserProfile user2);
}
