package com.opencode.alumxbackend.connection.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opencode.alumxbackend.connection.service.ConnectionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    @PostMapping("/{targetUserId}/connect")
    public ResponseEntity<?> postMethodName(@PathVariable Long targetUserId, @RequestHeader("X-USER-ID") Long userId) {
        
        connectionService.sendConnectionRequest(userId, targetUserId);
        return ResponseEntity.ok("Connection request sent");
    }
    
    
}
