package com.opencode.alumxbackend.connection.service;

public interface ConnectionService {
    
    void sendConnectionRequest(Long senderId, Long receiverId);
}
