package com.yuriyuri.ws;

import com.yuriyuri.config.WebSocketEndpointConfigurator;
import com.yuriyuri.entity.User;
import com.yuriyuri.mapper.UserMapper;
import com.yuriyuri.util.JwtUtil;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 前端连上后，服务器只用来<strong>推送通知</strong>（新评论、新私信）。
 * 发私信走 HTTP：POST /privateMessage/send
 */
@Component
@ServerEndpoint(value = "/ws/private", configurator = WebSocketEndpointConfigurator.class)
public class PrivateChatWebSocketEndpoint {

    private static final Logger log = LoggerFactory.getLogger(PrivateChatWebSocketEndpoint.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WebSocketSessionRegistry webSocketSessionRegistry;

    /**
     * 连接开启
     * @param session
     */
    @OnOpen
    public void onOpen(Session session) {
        //从session中获取token并简单校验
        Map<String, List<String>> params = session.getRequestParameterMap();
        List<String> tokens = params.get("token");
        if (tokens == null || tokens.isEmpty()) {
            closeSession(session);
            return;
        }
        //解析token
        Map<String, Object> claims = JwtUtil.parseToken(tokens.get(0));
        Long userId = ((Number) claims.get("id")).longValue();
        User user = userMapper.selectById(userId);
        //如果被封或者不存在就不能连接
        if (user == null || user.getStatus() == null || user.getStatus() == 0) {
            closeSession(session);
            return;
        }
        //将用户id存入session并注册对象
        session.getUserProperties().put("userId", userId);
        webSocketSessionRegistry.register(userId, session);
        log.info("WebSocket 已连接，用户ID: {}", userId);
    }

    /**
     * 连接关闭
     * @param session
     */
    @OnClose
    public void onClose(Session session) {
        Object uid = session.getUserProperties().get("userId");
        if (uid instanceof Long userId) {
            //如果下线了，检测uid里是否包含userId
            //如果包含，则连接关闭，集合中移除该用户的session
            webSocketSessionRegistry.unregister(userId, session);
            log.info("WebSocket 已断开，用户ID: {}", userId);
        }
    }

    /**
     * 异常连接
     * @param session
     * @param thr
     */
    @OnError
    public void onError(Session session, Throwable thr) {
        log.debug("WebSocket 异常: {}", thr.getMessage());
    }

    //无效/非法连接时主动关闭
    private void closeSession(Session session) {
        try {
            session.close();
        } catch (Exception ignored) {
        }
    }
}
