package com.yuriyuri.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriyuri.common.BusinessException;
import com.yuriyuri.dto.message.PrivateMessageVO;
import com.yuriyuri.dto.message.PrivateSessionRequest;
import com.yuriyuri.dto.message.PrivateSessionSummaryVO;
import com.yuriyuri.dto.message.PrivateSessionVO;
import com.yuriyuri.entity.FoundItem;
import com.yuriyuri.entity.LostItem;
import com.yuriyuri.entity.PrivateMessage;
import com.yuriyuri.entity.User;
import com.yuriyuri.mapper.FoundMapper;
import com.yuriyuri.mapper.LostMapper;
import com.yuriyuri.mapper.PrivateMessageMapper;
import com.yuriyuri.mapper.UserMapper;
import com.yuriyuri.service.PrivateMessageService;
import com.yuriyuri.util.PrivateSessionUtil;
import com.yuriyuri.ws.WebSocketSessionRegistry;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PrivateMessageServiceImpl implements PrivateMessageService {

    private static final int MAX_CONTENT = 2000;

    @Autowired
    private PrivateMessageMapper privateMessageMapper;

    @Autowired
    private LostMapper lostMapper;

    @Autowired
    private FoundMapper foundMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WebSocketSessionRegistry webSocketSessionRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public PrivateSessionVO ensureSession(Long userId, PrivateSessionRequest req) {
        if (userId.equals(req.getPeerId())) {
            throw new BusinessException("不能与自己私聊");
        }
        Long ownerId = resolvePostOwner(req.getPostId(), req.getPostType());
        if (!userId.equals(ownerId) && !req.getPeerId().equals(ownerId)) {
            throw new BusinessException("仅支持与发帖人就该帖子私聊");
        }
        String sessionId = PrivateSessionUtil.buildSessionId(req.getPostType(), req.getPostId(), userId, req.getPeerId());
        User peer = userMapper.selectById(req.getPeerId());
        if (peer == null) {
            throw new BusinessException("对方用户不存在");
        }
        String display = StringUtils.hasText(peer.getNickname()) ? peer.getNickname() : peer.getUsername();
        return new PrivateSessionVO(sessionId, req.getPostId(), req.getPostType(), req.getPeerId(), display);
    }

    private Long resolvePostOwner(Long postId, String postType) {
        if ("lost".equals(postType)) {
            LostItem item = lostMapper.selectById(postId);
            if (item == null) {
                throw new BusinessException("失物贴不存在");
            }
            return item.getUserId();
        }
        if ("found".equals(postType)) {
            FoundItem item = foundMapper.selectById(postId);
            if (item == null) {
                throw new BusinessException("拾物贴不存在");
            }
            return item.getUserId();
        }
        throw new BusinessException("无效的帖子类型");
    }

    @Override
    public List<PrivateMessageVO> listHistory(Long userId, String sessionId) {
        PrivateSessionUtil.validateSessionShape(sessionId);
        if (!PrivateSessionUtil.userInSession(sessionId, userId)) {
            throw new BusinessException("无权查看该会话");
        }
        LambdaQueryWrapper<PrivateMessage> w = new LambdaQueryWrapper<>();
        w.eq(PrivateMessage::getSessionId, sessionId)
                .orderByAsc(PrivateMessage::getCreateTime);
        List<PrivateMessage> list = privateMessageMapper.selectList(w);
        List<PrivateMessageVO> voList = new ArrayList<>();
        for (PrivateMessage m : list) {
            voList.add(toVo(m));
        }
        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSessionRead(Long userId, String sessionId) {
        PrivateSessionUtil.validateSessionShape(sessionId);
        if (!PrivateSessionUtil.userInSession(sessionId, userId)) {
            throw new BusinessException("无权操作该会话");
        }
        LambdaUpdateWrapper<PrivateMessage> uw = new LambdaUpdateWrapper<>();
        uw.eq(PrivateMessage::getSessionId, sessionId)
                .eq(PrivateMessage::getReceiverId, userId)
                .eq(PrivateMessage::getIsRead, 0)
                .set(PrivateMessage::getIsRead, 1);
        privateMessageMapper.update(null, uw);
    }

    @Override
    public Long countUnread(Long userId) {
        LambdaQueryWrapper<PrivateMessage> w = new LambdaQueryWrapper<>();
        w.eq(PrivateMessage::getReceiverId, userId).eq(PrivateMessage::getIsRead, 0);
        return privateMessageMapper.selectCount(w);
    }

    @Override
    public List<PrivateSessionSummaryVO> listSessionSummaries(Long userId) {
        LambdaQueryWrapper<PrivateMessage> w = new LambdaQueryWrapper<>();
        w.and(q -> q.eq(PrivateMessage::getSenderId, userId).or().eq(PrivateMessage::getReceiverId, userId))
                .orderByDesc(PrivateMessage::getCreateTime);
        List<PrivateMessage> all = privateMessageMapper.selectList(w);
        Set<String> seen = new HashSet<>();
        List<PrivateSessionSummaryVO> result = new ArrayList<>();
        for (PrivateMessage m : all) {
            if (!seen.add(m.getSessionId())) {
                continue;
            }
            PrivateSessionSummaryVO s = new PrivateSessionSummaryVO();
            s.setSessionId(m.getSessionId());
            if (!PrivateSessionUtil.validShape(m.getSessionId())) {
                continue;
            }
            s.setPostId(PrivateSessionUtil.getPostId(m.getSessionId()));
            s.setPostType(PrivateSessionUtil.getPostType(m.getSessionId()));
            long peer = m.getSenderId().equals(userId) ? m.getReceiverId() : m.getSenderId();
            s.setPeerId(peer);
            User u = userMapper.selectById(peer);
            if (u != null) {
                s.setPeerDisplayName(StringUtils.hasText(u.getNickname()) ? u.getNickname() : u.getUsername());
            } else {
                s.setPeerDisplayName("用户" + peer);
            }
            s.setLastContent(m.getContent());
            s.setLastTime(m.getCreateTime());
            LambdaQueryWrapper<PrivateMessage> uw = new LambdaQueryWrapper<>();
            uw.eq(PrivateMessage::getSessionId, m.getSessionId())
                    .eq(PrivateMessage::getReceiverId, userId)
                    .eq(PrivateMessage::getIsRead, 0);
            s.setUnreadCount(privateMessageMapper.selectCount(uw));
            result.add(s);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrivateMessageVO sendMessage(Long senderId, String sessionId, String content) {
        if (!StringUtils.hasText(content) || content.length() > MAX_CONTENT) {
            throw new BusinessException("消息内容长度不合法");
        }
        PrivateSessionUtil.validateSessionShape(sessionId);
        if (!PrivateSessionUtil.userInSession(sessionId, senderId)) {
            throw new BusinessException("无权在该会话中发言");
        }
        long receiverId = PrivateSessionUtil.getPeerUserId(sessionId, senderId);
        PrivateMessage row = new PrivateMessage();
        row.setSessionId(sessionId);
        row.setSenderId(senderId);
        row.setReceiverId(receiverId);
        row.setContent(content.trim());
        row.setIsRead(0);
        row.setCreateTime(LocalDateTime.now());
        privateMessageMapper.insert(row);
        PrivateMessageVO vo = toVo(row);
        pushNewPrivateMessage(vo);
        return vo;
    }

    @SneakyThrows
    private void pushNewPrivateMessage(PrivateMessageVO vo) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "newPrivateMessage");
        payload.put("message", vo);
        String json = objectMapper.writeValueAsString(payload);
        webSocketSessionRegistry.sendText(vo.getSenderId(), json);
        webSocketSessionRegistry.sendText(vo.getReceiverId(), json);
    }

    private PrivateMessageVO toVo(PrivateMessage m) {
        PrivateMessageVO vo = new PrivateMessageVO();
        BeanUtils.copyProperties(m, vo);
        return vo;
    }
}
