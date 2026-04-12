package com.yuriyuri.ws;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import jakarta.websocket.Session;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class WebSocketSessionRegistry {

    private final Map<Long, Set<Session>> userIdToSessions = new ConcurrentHashMap<>();

    public void register(Long userId, Session session) {
        userIdToSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public void unregister(Long userId, Session session) {
        Set<Session> set = userIdToSessions.get(userId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                userIdToSessions.remove(userId);
            }
        }
    }

    @SneakyThrows
    public void sendText(Long userId, String text) {
        Set<Session> set = userIdToSessions.get(userId);
        if (set == null || set.isEmpty()) {
            return;
        }
        for (Session s : set) {
            if (s != null && s.isOpen()) {
                synchronized (s) {
                    s.getBasicRemote().sendText(text);
                }
            }
        }
    }
}
