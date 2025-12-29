package com.opencode.alumxbackend.users.controller;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opencode.alumxbackend.users.dto.UserProfileDTO;
import com.opencode.alumxbackend.users.dto.UserProfileUpdateRequestDto;
import com.opencode.alumxbackend.users.dto.UserRequest;
import com.opencode.alumxbackend.users.dto.UserResponseDto;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService; // use service instead of repository
//     private static final String DUMMY_TOKEN = "alumx-dev-token";
    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    @PostMapping
    public ResponseEntity<?> createUser(
            @RequestHeader(value = "X-DUMMY-TOKEN", required = false) String token,
            @Valid @RequestBody UserRequest request) {

//        if (token == null || !token.equals(DUMMY_TOKEN)) {
//            logger.warning("Unauthorized access attempt to Dev API. Missing or invalid token.");
//            throw new UnauthorizedAccessException("Invalid or missing X-DUMMY-TOKEN header");
//        }

        logger.info(String.format("Creating new user: %s with role: %s", request.getUsername(), request.getRole()));
        User user = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "User created successfully via DEV API",
                "userId", user.getId(),
                "role", user.getRole().name()
        ));
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Long userId){
        try {
            return ResponseEntity.ok(userService.getUserProfile(userId));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        logger.info("Fetching all users");
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{userId}/profile")
    public ResponseEntity<UserProfileDTO> updateUserProfile(
            @PathVariable Long userId,
            @RequestBody UserProfileUpdateRequestDto request
    ) {
        UserProfileDTO updatedUser = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(updatedUser);
    }
}
