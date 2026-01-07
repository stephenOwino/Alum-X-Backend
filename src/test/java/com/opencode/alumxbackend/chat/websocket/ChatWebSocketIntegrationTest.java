package com.opencode.alumxbackend.chat.websocket;

import com.opencode.alumxbackend.chat.dto.ChatSendResponse;
import com.opencode.alumxbackend.chat.repository.ChatRepository;
import com.opencode.alumxbackend.chat.repository.MessageRepository;
import com.opencode.alumxbackend.chat.service.ChatService;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.model.UserRole;
import com.opencode.alumxbackend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChatWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatService chatService;

    private WebSocketStompClient stompClient;
    private String wsUrl;

    @BeforeEach
    void setUp() {
        wsUrl = "ws://localhost:" + port + "/ws";

        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        messageRepository.deleteAll();
        chatRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testMessageBroadcastViaWebSocket() throws Exception {
        User alice = createTestUser("alice", "alice@test.com");
        User bob = createTestUser("bob", "bob@test.com");

        ChatSendResponse firstMessage = chatService.createMessage(alice.getId(), bob.getId(), "Initial message");
        Long chatId = firstMessage.getChatId();

        BlockingQueue<ChatSendResponse> receivedMessages = new ArrayBlockingQueue<>(1);

        StompSession session = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        session.subscribe("/topic/chat/" + chatId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatSendResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedMessages.add((ChatSendResponse) payload);
            }
        });

        Thread.sleep(500);

        chatService.createMessage(alice.getId(), bob.getId(), "Hello Bob!");

        ChatSendResponse received = receivedMessages.poll(5, TimeUnit.SECONDS);

        assertThat(received).isNotNull();
        assertThat(received.getContent()).isEqualTo("Hello Bob!");
        assertThat(received.getSenderUsername()).isEqualTo("alice");
        assertThat(received.getReceiverUsername()).isEqualTo("bob");
        assertThat(received.getChatId()).isEqualTo(chatId);
        assertThat(session.isConnected()).isTrue();

        session.disconnect();
    }

    @Test
    void testMultipleClientsReceiveSameMessage() throws Exception {
        User alice = createTestUser("alice", "alice@test.com");
        User bob = createTestUser("bob", "bob@test.com");

        ChatSendResponse firstMessage = chatService.createMessage(alice.getId(), bob.getId(), "Initial");
        Long chatId = firstMessage.getChatId();

        BlockingQueue<ChatSendResponse> aliceMessages = new ArrayBlockingQueue<>(1);
        BlockingQueue<ChatSendResponse> bobMessages = new ArrayBlockingQueue<>(1);

        StompSession aliceSession = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);
        StompSession bobSession = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        aliceSession.subscribe("/topic/chat/" + chatId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatSendResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                aliceMessages.add((ChatSendResponse) payload);
            }
        });

        bobSession.subscribe("/topic/chat/" + chatId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatSendResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                bobMessages.add((ChatSendResponse) payload);
            }
        });

        Thread.sleep(500);

        assertThat(aliceSession.isConnected()).isTrue();
        assertThat(bobSession.isConnected()).isTrue();

        chatService.createMessage(alice.getId(), bob.getId(), "Both should receive this");

        ChatSendResponse aliceReceived = aliceMessages.poll(5, TimeUnit.SECONDS);
        ChatSendResponse bobReceived = bobMessages.poll(5, TimeUnit.SECONDS);

        assertThat(aliceReceived).isNotNull();
        assertThat(bobReceived).isNotNull();
        assertThat(aliceReceived.getContent()).isEqualTo("Both should receive this");
        assertThat(bobReceived.getContent()).isEqualTo("Both should receive this");
        assertThat(aliceReceived.getMessageId()).isEqualTo(bobReceived.getMessageId());

        aliceSession.disconnect();
        bobSession.disconnect();
    }

    @Test
    void testTopicIsolation() throws Exception {
        User alice = createTestUser("alice", "alice@test.com");
        User bob = createTestUser("bob", "bob@test.com");
        User charlie = createTestUser("charlie", "charlie@test.com");

        ChatSendResponse aliceBobChat = chatService.createMessage(alice.getId(), bob.getId(), "Alice to Bob");
        Long aliceBobChatId = aliceBobChat.getChatId();

        ChatSendResponse aliceCharlieChat = chatService.createMessage(alice.getId(), charlie.getId(), "Alice to Charlie");
        Long aliceCharlieChatId = aliceCharlieChat.getChatId();

        BlockingQueue<ChatSendResponse> aliceBobMessages = new ArrayBlockingQueue<>(1);
        BlockingQueue<ChatSendResponse> aliceCharlieMessages = new ArrayBlockingQueue<>(1);

        StompSession session1 = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);
        StompSession session2 = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        session1.subscribe("/topic/chat/" + aliceBobChatId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatSendResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                aliceBobMessages.add((ChatSendResponse) payload);
            }
        });

        session2.subscribe("/topic/chat/" + aliceCharlieChatId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatSendResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                aliceCharlieMessages.add((ChatSendResponse) payload);
            }
        });

        Thread.sleep(500);

        assertThat(session1.isConnected()).isTrue();
        assertThat(session2.isConnected()).isTrue();
        assertThat(aliceBobChatId).isNotEqualTo(aliceCharlieChatId);

        session1.disconnect();
        session2.disconnect();
    }

    private User createTestUser(String username, String email) {
        User user = User.builder()
                .username(username)
                .name(username)
                .email(email)
                .passwordHash("hashed_password")
                .role(UserRole.STUDENT)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }
}
