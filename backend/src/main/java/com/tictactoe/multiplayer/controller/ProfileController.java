package com.tictactoe.multiplayer.controller;

import com.tictactoe.multiplayer.model.GameHistory;
import com.tictactoe.multiplayer.model.Friendship;
import com.tictactoe.multiplayer.model.FriendshipStatus;
import com.tictactoe.multiplayer.model.UserProfile;
import com.tictactoe.multiplayer.repository.FriendshipRepository;
import com.tictactoe.multiplayer.repository.GameHistoryRepository;
import com.tictactoe.multiplayer.repository.UserProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final UserProfileRepository userProfileRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final FriendshipRepository friendshipRepository;

    public ProfileController(UserProfileRepository userProfileRepository,
                             GameHistoryRepository gameHistoryRepository,
                             FriendshipRepository friendshipRepository) {
        this.userProfileRepository = userProfileRepository;
        this.gameHistoryRepository = gameHistoryRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<?> getProfile(@PathVariable String playerId) {
        Optional<UserProfile> profileOpt = userProfileRepository.findByPlayerId(playerId);

        if (profileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserProfile profile = profileOpt.get();

        // Get 10 recent games
        List<GameHistory> recentGames = gameHistoryRepository
                .findByUserProfileOrderByPlayedAtDesc(profile, PageRequest.of(0, 10));

        // Get friends
        List<Friendship> friends = friendshipRepository.findByUserAndStatus(profile, FriendshipStatus.ACCEPTED);

        // Map everything for frontend
        Map<String, Object> response = new HashMap<>();
        response.put("playerId", profile.getPlayerId());
        response.put("displayName", profile.getDisplayName());
        response.put("avatarColor", profile.getAvatarColor());
        response.put("createdAt", profile.getCreatedAt());
        response.put("lastOnline", profile.getLastOnline());
        response.put("lastPlayed", profile.getLastPlayed());
        response.put("wins", profile.getWins());
        response.put("losses", profile.getLosses());
        response.put("draws", profile.getDraws());
        response.put("totalGames", profile.getTotalGames());
        response.put("rank", profile.getRank().name());

        // Format friends and games
        List<Map<String, Object>> formattedFriends = friends.stream().map(f -> {
            UserProfile friend = f.getSender().getId().equals(profile.getId()) ? f.getReceiver() : f.getSender();
            return Map.<String, Object>of(
                "playerId", friend.getPlayerId(),
                "displayName", friend.getDisplayName(),
                "avatarColor", friend.getAvatarColor(),
                "rank", friend.getRank().name(),
                "lastOnline", friend.getLastOnline() != null ? friend.getLastOnline() : "Unknown"
            );
        }).collect(Collectors.toList());

        List<Map<String, Object>> formattedGames = recentGames.stream().map(g -> {
            boolean isX = g.getPlayerX().getId().equals(profile.getId());
            UserProfile opponent = isX ? g.getPlayerO() : g.getPlayerX();
            String result = "DRAW";
            if (g.getWinner() != null) {
                result = g.getWinner().getId().equals(profile.getId()) ? "WIN" : "LOSS";
            }
            return Map.<String, Object>of(
                "opponentName", opponent.getDisplayName(),
                "opponentId", opponent.getPlayerId(),
                "result", result,
                "playedAt", g.getPlayedAt(),
                "roomId", g.getRoomId()
            );
        }).collect(Collectors.toList());

        response.put("friends", formattedFriends);
        response.put("gameHistory", formattedGames);

        return ResponseEntity.ok(response);
    }
}
