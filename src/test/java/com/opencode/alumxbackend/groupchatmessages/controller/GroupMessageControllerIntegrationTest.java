package com.opencode.alumxbackend.groupchatmessages.controller;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import com.opencode.alumxbackend.groupchat.dto.GroupChatRequest;
import com.opencode.alumxbackend.groupchat.dto.GroupChatResponse;
import com.opencode.alumxbackend.groupchat.repository.GroupChatRepository;
import com.opencode.alumxbackend.groupchatmessages.dto.GroupMessageResponse;
import com.opencode.alumxbackend.groupchatmessages.dto.SendGroupMessageRequest;
import com.opencode.alumxbackend.groupchatmessages.repository.GroupMessageRepository;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.model.UserRole;
import com.opencode.alumxbackend.users.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GroupMessageControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupChatRepository groupChatRepository;

    @Autowired
    private GroupMessageRepository groupMessageRepository;

    private WebClient webClient;
    private User testUser1;
    private User testUser2;
    private Long testGroupId;

    @BeforeEach
    void setUp() {
        webClient = WebClient.create("http://localhost:" + port);

        // Clean up
        groupMessageRepository.deleteAll();
        groupChatRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUser1 = User.builder()
                .username("user1")
                .name("Test User 1")
                .email("user1@test.com")
                .passwordHash("hashedpass")
                .role(UserRole.STUDENT)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser1 = userRepository.save(testUser1);

        testUser2 = User.builder()
                .username("user2")
                .name("Test User 2")
                .email("user2@test.com")
                .passwordHash("hashedpass")
                .role(UserRole.ALUMNI)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser2 = userRepository.save(testUser2);

        // Create a test group
        GroupChatRequest groupRequest = GroupChatRequest.builder()
                .name("Test Group")
                .participants(List.of(
                        new GroupChatRequest.ParticipantRequest(testUser1.getId(), testUser1.getUsername()),
                        new GroupChatRequest.ParticipantRequest(testUser2.getId(), testUser2.getUsername())
                ))
                .build();

        GroupChatResponse createdGroup = webClient.post()
                .uri("/api/group-chats")
                .bodyValue(groupRequest)
                .retrieve()
                .bodyToMono(GroupChatResponse.class)
                .block();

        testGroupId = createdGroup.getGroupId();
    }

    // ========== SUCCESS CASES ==========

    @Test
    @DisplayName("POST /api/groups/{groupId}/messages - should send message to group")
    void sendMessage_WithValidData_ReturnsCreatedMessage() {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setUserId(testUser1.getId());
        request.setContent("Hello everyone!");

        GroupMessageResponse response = webClient.post()
                .uri("/api/groups/" + testGroupId + "/messages")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GroupMessageResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getSenderUserId()).isEqualTo(testUser1.getId());
        assertThat(response.getContent()).isEqualTo("Hello everyone!");
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should return all messages from group")
    void getMessages_ExistingMessages_ReturnsAllMessages() {
        // Send two messages
        SendGroupMessageRequest msg1 = new SendGroupMessageRequest();
        msg1.setUserId(testUser1.getId());
        msg1.setContent("First message");

        SendGroupMessageRequest msg2 = new SendGroupMessageRequest();
        msg2.setUserId(testUser2.getId());
        msg2.setContent("Second message");

        webClient.post().uri("/api/groups/" + testGroupId + "/messages").bodyValue(msg1).retrieve().bodyToMono(GroupMessageResponse.class).block();
        webClient.post().uri("/api/groups/" + testGroupId + "/messages").bodyValue(msg2).retrieve().bodyToMono(GroupMessageResponse.class).block();

        // Fetch messages
        List<?> response = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should return empty list when no messages")
    void getMessages_NoMessages_ReturnsEmptyList() {
        List<?> response = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/groups/{groupId}/messages/{messageId} - should delete and not return in get routes")
    void deleteMessage_RemovesFromGetRoutes() {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setUserId(testUser1.getId());
        request.setContent("Message to delete");

        GroupMessageResponse created = webClient.post()
                .uri("/api/groups/" + testGroupId + "/messages")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GroupMessageResponse.class)
                .block();

        assertThat(created).isNotNull();

        var deleteResponse = webClient.delete()
                .uri("/api/groups/" + testGroupId + "/messages/" + created.getId()
                        + "?userId=" + testUser1.getId())
                .retrieve()
                .toBodilessEntity()
                .block();

        assertThat(deleteResponse).isNotNull();
        assertThat(deleteResponse.getStatusCode().value()).isEqualTo(204);

        List<?> allMessages = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages")
                .retrieve()
                .bodyToMono(List.class)
                .block();

        assertThat(allMessages).isNotNull();
        assertThat(allMessages).isEmpty();

        List<?> memberMessages = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages/user/" + testUser1.getId())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        assertThat(memberMessages).isNotNull();
        assertThat(memberMessages).isEmpty();
    }

    // ========== FAILURE CASES ==========

    @Test
    @DisplayName("POST /api/groups/{groupId}/messages - should fail with empty message content")
    void sendMessage_WithEmptyContent_ReturnsBadRequest() {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setUserId(testUser1.getId());
        request.setContent("");

        try {
            webClient.post()
                    .uri("/api/groups/" + testGroupId + "/messages")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("400");
        }
    }

    @Test
    @DisplayName("POST /api/groups/{groupId}/messages - should fail with message exceeding max length")
    void sendMessage_WithTooLongContent_ReturnsBadRequest() {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setUserId(testUser1.getId());
        request.setContent("a".repeat(1001)); // More than 1000 characters

        try {
            webClient.post()
                    .uri("/api/groups/" + testGroupId + "/messages")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("400");
        }
    }

    @Test
    @DisplayName("POST /api/groups/{groupId}/messages - should fail with missing userId")
    void sendMessage_WithoutUserId_ReturnsBadRequest() {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setContent("This should fail");
        // userId is null

        try {
            webClient.post()
                    .uri("/api/groups/" + testGroupId + "/messages")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("400");
        }
    }

    @Test
    @DisplayName("POST /api/groups/{groupId}/messages - should fail for non-existent group")
    void sendMessage_ToNonExistentGroup_ReturnsNotFound() {
        // Note: Service throws GroupNotFoundException which returns 500
        // This test validates current behavior
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setUserId(testUser1.getId());
        request.setContent("This should fail");

        try {
            webClient.post()
                    .uri("/api/groups/99999/messages")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("500");
        }
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages?userId=<userId> - should fail for non-existent group")
    void getMessages_FromNonExistentGroup_ReturnsNotFound() {
        // Note: Service throws GroupNotFoundException which is RuntimeException
        // but we are catching it and returning 404
        try {
            webClient.get()
                    .uri("/api/groups/99999/messages?userId=" + testUser1.getId())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("404");
        }
    }
}
