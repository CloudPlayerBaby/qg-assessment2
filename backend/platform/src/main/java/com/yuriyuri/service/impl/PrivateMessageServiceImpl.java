package com.yuriyuri.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriyuri.common.BusinessException;
import com.yuriyuri.constant.private_message.IsRead;
import com.yuriyuri.dto.message.PrivateChatRow;
import com.yuriyuri.dto.message.PrivateMessageSendRequest;
import com.yuriyuri.entity.FoundItem;
import com.yuriyuri.entity.LostItem;
import com.yuriyuri.entity.PrivateMessage;
import com.yuriyuri.entity.User;
import com.yuriyuri.mapper.FoundMapper;
import com.yuriyuri.mapper.LostMapper;
import com.yuriyuri.mapper.PrivateMessageMapper;
import com.yuriyuri.mapper.UserMapper;
import com.yuriyuri.service.PrivateMessageService;
import com.yuriyuri.ws.WebSocketSessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PrivateMessageServiceImpl implements PrivateMessageService {

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

    /**
     * 发送私信
     *
     * @param senderId
     * @param req
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void send(Long senderId, PrivateMessageSendRequest req) {
        //简单校验一下
        if (senderId.equals(req.getReceiverId())) {
            throw new BusinessException("不能给自己发私信");
        }
        Long ownerId = getPostOwnerId(req.getPostId(), req.getPostType());
        if (!senderId.equals(ownerId) && !req.getReceiverId().equals(ownerId)) {
            throw new BusinessException("只能和发帖人就该帖子互发私信");
        }
        String content = req.getContent() == null ? "" : req.getContent().trim();
        //我这里content的类型就是text，用一下hasText校验
        if (!StringUtils.hasText(content) || content.length() > 2000) {
            throw new BusinessException("私信内容不能为空且不超过2000字");
        }

        //获取sessionId
        String sessionId = buildSessionId(req.getPostType(), req.getPostId(), senderId, req.getReceiverId());
        //剩下的就是简单的insert
        PrivateMessage privateMessage = new PrivateMessage();
        privateMessage.setSessionId(sessionId);
        privateMessage.setSenderId(senderId);
        privateMessage.setReceiverId(req.getReceiverId());
        privateMessage.setContent(content);
        privateMessage.setIsRead(IsRead.UNREAD);
        privateMessage.setCreateTime(LocalDateTime.now());
        privateMessageMapper.insert(privateMessage);

        //WebSocket推送消息
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "newPm");
        payload.put("postId", req.getPostId());
        payload.put("postType", req.getPostType());
        payload.put("peerId", senderId);
        try {
            //转json
            String json = objectMapper.writeValueAsString(payload);
            //推送
            webSocketSessionRegistry.sendText(req.getReceiverId(), json);
        } catch (Exception ignored) {
        }
    }

    /**
     * 获取列表
     *
     * @param userId
     * @param postId
     * @param postType
     * @param peerId
     * @return
     */
    @Override
    public List<PrivateMessage> list(Long userId, Long postId, String postType, Long peerId) {
        //简单校验
        if (userId.equals(peerId)) {
            throw new BusinessException("参数错误");
        }

        Long ownerId = getPostOwnerId(postId, postType);
        if (!userId.equals(ownerId) && !peerId.equals(ownerId)) {
            throw new BusinessException("无权查看");
        }

        //创建sessionId
        String sessionId = buildSessionId(postType, postId, userId, peerId);
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();

        //这里是正序获取消息
        wrapper.eq(PrivateMessage::getSessionId, sessionId)
                .orderByAsc(PrivateMessage::getCreateTime);
        return privateMessageMapper.selectList(wrapper);
    }

    /**
     * 标记已读
     *
     * @param userId
     * @param postId
     * @param postType
     * @param peerId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long userId, Long postId, String postType, Long peerId) {
        //简单校验一下
        if (userId.equals(peerId)) {
            throw new BusinessException("参数错误");
        }
        Long ownerId = getPostOwnerId(postId, postType);
        if (!userId.equals(ownerId) && !peerId.equals(ownerId)) {
            throw new BusinessException("无权操作");
        }

        //创建sessionId并进行查询
        String sessionId = buildSessionId(postType, postId, userId, peerId);
        //把未读变成已读，写一个update private_message set is_read=1 where receiver_id=? and is_read=? session_id=?
        LambdaUpdateWrapper<PrivateMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PrivateMessage::getSessionId, sessionId)
                .eq(PrivateMessage::getReceiverId, userId)
                .eq(PrivateMessage::getIsRead, IsRead.UNREAD)
                .set(PrivateMessage::getIsRead, IsRead.READ);
        privateMessageMapper.update(wrapper);
    }

    /**
     * 获取未读数
     *
     * @param userId
     * @return
     */
    @Override
    public Long countUnread(Long userId) {
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateMessage::getReceiverId, userId)
                .eq(PrivateMessage::getIsRead, IsRead.UNREAD);
        return privateMessageMapper.selectCount(wrapper);
    }

    /**
     * 获取会话列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<PrivateChatRow> myChats(Long userId) {
        //要获取消息列表，假设a发给b，那么a、b都要获取消息列表
        //写一个select * from private_message where (id=sender_id or id=receiver_id) order by create_time desc;
        LambdaQueryWrapper<PrivateMessage> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.and(q -> q.eq(PrivateMessage::getSenderId, userId).or().eq(PrivateMessage::getReceiverId, userId))
                .orderByDesc(PrivateMessage::getCreateTime);
        List<PrivateMessage> all = privateMessageMapper.selectList(wrapper1);

        //从用户参与的所有私信记录中，提取每个对话的最新一条消息作为摘要，同时统计每个对话中当前用户未读的消息数，最终返回一个私聊会话列表

        //每个会话预览只需要最后一条聊天
        Set<String> seen = new HashSet<>();
        List<PrivateChatRow> rows = new ArrayList<>();
        //遍历所有的消息
        for (PrivateMessage privateMessage : all) {
            //获取sessionId
            String sessionId = privateMessage.getSessionId();
            //如果seen已经包含了这条sessionId，就直接跳过
            if (sessionId == null || seen.contains(sessionId)) {
                continue;
            }

            seen.add(sessionId);

            //截取一下，格式lost_1_1_5
            String[] p = sessionId.split("_");
            PrivateChatRow row = new PrivateChatRow();
            row.setPostType(p[0]);
            row.setPostId(Long.parseLong(p[1]));

            //最后一条消息是谁发的？
            long peerId = privateMessage.getSenderId().equals(userId) ? privateMessage.getReceiverId() : privateMessage.getSenderId();
            row.setPeerId(peerId);

            User user = userMapper.selectById(peerId);
            //设置对方的昵称
            if (user != null) {
                row.setPeerName(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
            } else {
                row.setPeerName("用户" + peerId);
            }

            row.setLastContent(privateMessage.getContent());
            row.setLastTime(privateMessage.getCreateTime());

            //写一个select count(*) from private_message where session_id=? and receiver_id=? and is_read=0 获取未读数
            LambdaQueryWrapper<PrivateMessage> wrapper2 = new LambdaQueryWrapper<>();
            wrapper2.eq(PrivateMessage::getSessionId, sessionId)
                    .eq(PrivateMessage::getReceiverId, userId)
                    .eq(PrivateMessage::getIsRead, IsRead.UNREAD);
            row.setUnreadCount(privateMessageMapper.selectCount(wrapper2));
            rows.add(row);
        }
        return rows;
    }

    //以下为方法

    //创建sessionId的方法，保证sessionId唯一，也用于查询
    private String buildSessionId(String postType, Long postId, Long userId1, Long userId2) {
        long a = Math.min(userId1, userId2);
        long b = Math.max(userId1, userId2);
        return postType + "_" + postId + "_" + a + "_" + b;
    }

    //获取楼主id的方法
    private Long getPostOwnerId(Long postId, String postType) {
        //类型校验和存在校验
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
}
