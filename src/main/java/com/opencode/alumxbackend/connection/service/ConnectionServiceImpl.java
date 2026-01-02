package com.opencode.alumxbackend.connection.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencode.alumxbackend.connection.model.Connection;
import com.opencode.alumxbackend.connection.model.ConnectionStatus;
import com.opencode.alumxbackend.connection.repository.ConnectionRepository;
import com.opencode.alumxbackend.users.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ConnectionServiceImpl implements ConnectionService {
    
    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;

    @Override
    public void sendConnectionRequest(Long senderId, Long receiverId) {

        if (!userRepository.existsById(senderId)) {
            throw new EntityNotFoundException("Sender not found");
        }

        if (!userRepository.existsById(receiverId)) {
            throw new EntityNotFoundException("Receiver not found");
        }

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot connect with yourself");
        }

        Optional<Connection> existing = connectionRepository.findByReceiverIdAndSenderId(senderId, receiverId);

        if (existing.isPresent()) {
            if (existing.get().getStatus() == ConnectionStatus.ACCEPTED) {
                throw new IllegalStateException("User is already connected");
            }
            if (existing.get().getStatus() == ConnectionStatus.PENDING) {
                throw new IllegalStateException("Connection request already sent");
            }
        }

        Connection connection = Connection.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .build();

        connectionRepository.save(connection);
    }
}
