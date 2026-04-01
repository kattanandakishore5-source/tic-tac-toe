package com.tictactoe.multiplayer.controller;

import com.tictactoe.multiplayer.model.Friendship;
import com.tictactoe.multiplayer.model.FriendshipStatus;
import com.tictactoe.multiplayer.model.UserProfile;
import com.tictactoe.multiplayer.repository.FriendshipRepository;
import com.tictactoe.multiplayer.repository.UserProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*")
public class FriendController {

    private final FriendshipRepository friendshipRepository;
    private final UserProfileRepository userProfileRepository;

    public FriendController(FriendshipRepository friendshipRepository,
                            UserProfileRepository userProfileRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(@RequestBody Map<String, String> body) {
        String senderId = body.get("senderId");
        String receiverId = body.get("receiverId");

        if (senderId.equals(receiverId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot add yourself"));
        }

        Optional<UserProfile> senderOpt = userProfileRepository.findByPlayerId(senderId);
        Optional<UserProfile> receiverOpt = userProfileRepository.findByPlayerId(receiverId);

        if (senderOpt.isEmpty() || receiverOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        UserProfile sender = senderOpt.get();
        UserProfile receiver = receiverOpt.get();

        Optional<Friendship> existing = friendshipRepository.findByUsers(sender, receiver);
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Friendship or request already exists"));
        }

        Friendship friendship = new Friendship();
        friendship.setSender(sender);
        friendship.setReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendshipRepository.save(friendship);

        return ResponseEntity.ok(Map.of("message", "Request sent"));
    }

    @PostMapping("/accept")
    public ResponseEntity<?> acceptRequest(@RequestBody Map<String, String> body) {
        return updateStatus(body, FriendshipStatus.ACCEPTED);
    }

    @PostMapping("/reject")
    public ResponseEntity<?> rejectRequest(@RequestBody Map<String, String> body) {
        // We can just delete or mark as BLOCKED/REJECTED. Deleting is easier for rejects constraint wise
        String senderId = body.get("senderId");
        String receiverId = body.get("receiverId");

        Optional<UserProfile> senderOpt = userProfileRepository.findByPlayerId(senderId);
        Optional<UserProfile> receiverOpt = userProfileRepository.findByPlayerId(receiverId);

        if (senderOpt.isPresent() && receiverOpt.isPresent()) {
             Optional<Friendship> f = friendshipRepository.findByUsers(senderOpt.get(), receiverOpt.get());
             f.ifPresent(friendshipRepository::delete);
        }
        return ResponseEntity.ok(Map.of("message", "Request rejected"));
    }

    private ResponseEntity<?> updateStatus(Map<String, String> body, FriendshipStatus status) {
        String senderId = body.get("senderId");
        String receiverId = body.get("receiverId");

        Optional<UserProfile> senderOpt = userProfileRepository.findByPlayerId(senderId);
        Optional<UserProfile> receiverOpt = userProfileRepository.findByPlayerId(receiverId);

        if (senderOpt.isEmpty() || receiverOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "User not found"));

        Optional<Friendship> f = friendshipRepository.findByUsers(senderOpt.get(), receiverOpt.get());
        if (f.isPresent()) {
            Friendship friendship = f.get();
            friendship.setStatus(status);
            friendshipRepository.save(friendship);
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Request not found"));
    }

    @GetMapping("/{playerId}/pending")
    public ResponseEntity<?> getPendingRequests(@PathVariable String playerId) {
        Optional<UserProfile> profileOpt = userProfileRepository.findByPlayerId(playerId);
        if (profileOpt.isEmpty()) return ResponseEntity.notFound().build();

        List<Friendship> pending = friendshipRepository.findByReceiverAndStatus(profileOpt.get(), FriendshipStatus.PENDING);
        
        List<Map<String, Object>> result = pending.stream().map(f -> Map.<String, Object>of(
            "playerId", f.getSender().getPlayerId(),
            "displayName", f.getSender().getDisplayName(),
            "avatarColor", f.getSender().getAvatarColor()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
