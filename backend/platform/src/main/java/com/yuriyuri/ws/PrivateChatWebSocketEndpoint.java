package com.yuriyuri.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriyuri.common.BusinessException;
import com.yuriyuri.config.WebSocketEndpointConfigurator;
import com.yuriyuri.entity.User;
import com.yuriyuri.mapper.UserMapper;
import com.yuriyuri.service.PrivateMessageService;
import com.yuriyuri.util.JwtUtil;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ServerEndpoint(value = "/ws/private", configurator = WebSocketEndpointConfigurator.class)
public class PrivateChatWebSocketEndpoint {

    private static final Logger log = LoggerFactory.getLogger(PrivateChatWebSocketEndpoint.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PrivateMessageService privateMessageService;

    @Autowired
    private WebSocketSessionRegistry webSocketSessionRegistry;

    @OnOpen
    public void onOpen(Session session) {
        Map<String, List<String>> params = session.getRequestParameterMap();
        List<String> tokens = params.get("token");
        if (tokens == null || tokens.isEmpty()) {
            closeSession(session);
            return;
        }
        Map<String, Object> claims = JwtUtil.parseToken(tokens.get(0));
        Long userId = ((Number) claims.get("id")).longValue();
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() == 0) {
            closeSession(session);
            return;
        }
        session.getUserProperties().put("userId", userId);
        webSocketSessionRegistry.register(userId, session);
        log.info("WebSocket 私聊连接已建立，用户ID: {}", userId);
    }

    @OnMessage
    @SneakyThrows
    public void onMessage(String text, Session session) {
        Object uidObj = session.getUserProperties().get("userId");
        if (!(uidObj instanceof Long userId)) {
            return;
        }
        JsonNode node = objectMapper.readTree(text);
        if (!"send".equals(node.path("type").asText())) {
            return;
        }
        String sessionId = node.path("sessionId").asText(null);
        String content = node.path("content").asText(null);
        try {
            privateMessageService.sendMessage(userId, sessionId, content);
        } catch (BusinessException e) {
            sendError(session, e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        Object uidObj = session.getUserProperties().get("userId");
        if (uidObj instanceof Long userId) {
            webSocketSessionRegistry.unregister(userId, session);
            log.info("WebSocket 私聊连接已关闭，用户ID: {}", userId);
        }
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        log.debug("WebSocket 错误: {}", thr.getMessage());
    }

    @SneakyThrows
    private void sendError(Session session, String msg) {
        if (!session.isOpen()) {
            return;
        }
        String json = objectMapper.writeValueAsString(Map.of("type", "error", "message", msg));
        synchronized (session) {
            session.getBasicRemote().sendText(json);
        }
    }

    @SneakyThrows
    private void closeSession(Session session) {
        session.close();
    }
}
