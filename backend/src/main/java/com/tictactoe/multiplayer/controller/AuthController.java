package com.tictactoe.multiplayer.controller;

import com.tictactoe.multiplayer.model.User;
import com.tictactoe.multiplayer.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.tictactoe.multiplayer.repository.UserProfileRepository userProfileRepository;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, com.tictactoe.multiplayer.repository.UserProfileRepository userProfileRepository) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userProfileRepository = userProfileRepository;
    }

    // ── Register ──────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username    = request.get("username");
        String rawPassword = request.get("password");

        if (username == null || rawPassword == null
                || username.trim().isEmpty() || rawPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username and password are required"));
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username already exists"));
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword)); // BCrypt hash

        userRepository.save(user);

        // Create User Profile
        String playerId = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        while (userProfileRepository.findByPlayerId(playerId).isPresent()) {
            playerId = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }

        String[] colors = {"#EF4444", "#3B82F6", "#10B981", "#F59E0B", "#8B5CF6", "#EC4899", "#06B6D4"};
        String avatarColor = colors[new java.util.Random().nextInt(colors.length)];

        com.tictactoe.multiplayer.model.UserProfile profile = com.tictactoe.multiplayer.model.UserProfile.builder()
                .user(user)
                .playerId(playerId)
                .displayName(username) // Default display name is username
                .avatarColor(avatarColor)
                .build();
        userProfileRepository.save(profile);

        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "playerId", playerId
        ));
    }

    // ── Login ─────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username    = request.get("username");
        String rawPassword = request.get("password");

        if (username == null || rawPassword == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username and password are required"));
        }

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Compare raw password with BCrypt hash in DB
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                Optional<com.tictactoe.multiplayer.model.UserProfile> profileOpt = userProfileRepository.findByUserId(user.getId());
                
                if (profileOpt.isPresent()) {
                    com.tictactoe.multiplayer.model.UserProfile profile = profileOpt.get();
                    profile.setLastOnline(java.time.Instant.now());
                    userProfileRepository.save(profile);

                    return ResponseEntity.ok(Map.of(
                        "message",  "Login successful",
                        "username", user.getUsername(),
                        "playerId", profile.getPlayerId()
                    ));
                } else {
                    return ResponseEntity.ok(Map.of(
                        "message",  "Login successful",
                        "username", user.getUsername()
                    ));
                }
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username or password"));
    }

    // ── Check username availability ───────────────────────────────────

    @GetMapping("/check/{username}")
    public ResponseEntity<?> checkUsername(@PathVariable String username) {
        boolean exists = userRepository.findByUsername(username).isPresent();
        return ResponseEntity.ok(Map.of("available", !exists));
    }
}
