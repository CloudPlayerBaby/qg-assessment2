package com.yuriyuri.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class WebSocketSessionRegistry {
    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionRegistry.class);

    //一个用户对应一个或多个session，整个集合包含了所有用户
    private final Map<Long, Set<Session>> userIdToSessions = new ConcurrentHashMap<>();

    /**
     * 上线注册
     * @param userId
     * @param session
     */
    public void register(Long userId, Session session) {
        //如果该用户还没有对应的Set，就创建一个新的CopyOnWriteArraySet，然后将新的session添加到这个用户的会话集合中
        userIdToSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    /**
     * 下线注销
     * @param userId
     * @param session
     */
    public void unregister(Long userId, Session session) {
        //找到该用户的会话集合，移除此session，如果移完后集合为空，说明该用户所有设备都已离线，就删除整个Key
        Set<Session> set = userIdToSessions.get(userId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                userIdToSessions.remove(userId);
            }
        }
    }

    /**
     * 发送消息
     * @param userId
     * @param text
     */
    public void sendText(Long userId, String text) {
        //找到该用户的所有会话，遍历每个会话
        Set<Session> set = userIdToSessions.get(userId);
        if (set == null || set.isEmpty()) {
            return;
        }
        //如果连接还开着，就发送消息
        for (Session s : set) {
            if (s != null && s.isOpen()) {
                try {
                    //处理并发情况
                    synchronized (s) {
                        s.getBasicRemote().sendText(text);
                    }
                } catch (IOException e) {
                    log.warn("推送失败 userId={}", userId, e);
                }
            }
        }
    }

}
