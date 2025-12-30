package com.opencode.alumxbackend.notifications;

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

import com.opencode.alumxbackend.notifications.dto.NotificationRequest;
import com.opencode.alumxbackend.notifications.dto.NotificationResponse;
import com.opencode.alumxbackend.notifications.repository.NotificationRepository;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.model.UserRole;
import com.opencode.alumxbackend.users.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class NotificationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    private WebClient webClient;
    private User testUser;

    @BeforeEach
    public void setUp() {
        webClient = WebClient.create("http://localhost:" + port);

        // cleanup: remove notifications first to avoid FK constraint when deleting users
        notificationRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .username("notifyuser")
                .name("Notify User")
                .email("notifyuser@example.com")
                .passwordHash("hashedpass")
                .role(UserRole.STUDENT)
                .profileCompleted(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("POST /api/notifications - create and return notification")
    void createNotification_validRequest_returnsCreated() {
        NotificationRequest req = new NotificationRequest();
        req.setUserId(testUser.getId());
        req.setType("INFO");
        req.setMessage("Test notification");
        req.setReferenceId(123L);

        NotificationResponse resp = webClient.post()
                .uri("/api/notifications")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(NotificationResponse.class)
                .block();

        assertThat(resp).isNotNull();
        assertThat(resp.getType()).isEqualTo("INFO");
        assertThat(resp.getMessage()).isEqualTo("Test notification");
        assertThat(resp.getId()).isNotNull();
    }

    @Test
    @DisplayName("POST /api/notifications - missing userId returns 400")
    void createNotification_missingUserId_returnsBadRequest() {
        NotificationRequest req = new NotificationRequest();
        req.setType("INFO");
        req.setMessage("Missing userId");

        try {
            webClient.post().uri("/api/notifications").bodyValue(req).retrieve().bodyToMono(String.class).block();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("400");
        }
    }

    @Test
    @DisplayName("POST /api/notifications - empty message returns 400")
    void createNotification_emptyMessage_returnsBadRequest() {
        NotificationRequest req = new NotificationRequest();
        req.setUserId(testUser.getId());
        req.setType("INFO");
        req.setMessage("");

        try {
            webClient.post().uri("/api/notifications").bodyValue(req).retrieve().bodyToMono(String.class).block();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("400");
        }
    }

    @Test
    @DisplayName("GET /api/notifications - returns notifications for user ordered newest first")
    void getNotifications_returnsOrderedNotifications() {
        // create two notifications
        NotificationRequest req1 = new NotificationRequest();
        req1.setUserId(testUser.getId());
        req1.setType("INFO");
        req1.setMessage("First notification");

        NotificationRequest req2 = new NotificationRequest();
        req2.setUserId(testUser.getId());
        req2.setType("ALERT");
        req2.setMessage("Second notification");

        webClient.post().uri("/api/notifications").bodyValue(req1).retrieve().bodyToMono(NotificationResponse.class).block();
        webClient.post().uri("/api/notifications").bodyValue(req2).retrieve().bodyToMono(NotificationResponse.class).block();

        List<?> list = webClient.get()
                .uri("/api/notifications?userId=" + testUser.getId())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        assertThat(list).isNotNull();
        assertThat(list.size()).isGreaterThanOrEqualTo(2);
    }
}
