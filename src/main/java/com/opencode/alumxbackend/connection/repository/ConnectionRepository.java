package com.opencode.alumxbackend.connection.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opencode.alumxbackend.connection.model.Connection;


public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    Optional<Connection> findByReceiverIdAndSenderId(Long senderId, Long receiverId);
}
