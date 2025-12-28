package com.opencode.alumxbackend.chat;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


//conversation between two users
@Entity
@Table(name = "chats",
        indexes = {
                @Index(name = "idx_user1_username", columnList = "user1Username"),
                @Index(name = "idx_user2_username", columnList = "user2Username")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_users", columnNames = {"user1Username", "user2Username"})
        }
        
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private Long id;
    @Column(nullable = false)
    private String user1Username;
    @Column(nullable = false)
    private String user2Username;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
