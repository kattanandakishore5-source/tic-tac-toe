package com.tictactoe.multiplayer.repository;

import com.tictactoe.multiplayer.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByPlayerId(String playerId);
    Optional<UserProfile> findByUserId(Long userId);
    Optional<UserProfile> findByDisplayName(String displayName);
}
