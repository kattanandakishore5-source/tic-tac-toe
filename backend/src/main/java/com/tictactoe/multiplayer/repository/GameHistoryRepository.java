package com.tictactoe.multiplayer.repository;

import com.tictactoe.multiplayer.model.GameHistory;
import com.tictactoe.multiplayer.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface GameHistoryRepository extends JpaRepository<GameHistory, Long> {
    
    @Query("SELECT g FROM GameHistory g WHERE g.playerX = :profile OR g.playerO = :profile ORDER BY g.playedAt DESC")
    List<GameHistory> findByUserProfileOrderByPlayedAtDesc(UserProfile profile, Pageable pageable);
}
