package com.yuriyuri.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuriyuri.common.BusinessException;
import com.yuriyuri.constant.comment.IsRead;
import com.yuriyuri.dto.comment.CommentRequest;
import com.yuriyuri.entity.Comment;
import com.yuriyuri.entity.FoundItem;
import com.yuriyuri.entity.LostItem;
import com.yuriyuri.mapper.CommentMapper;
import com.yuriyuri.mapper.FoundMapper;
import com.yuriyuri.mapper.LostMapper;
import com.yuriyuri.service.CommentService;
import com.yuriyuri.ws.WebSocketSessionRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private LostMapper lostMapper;

    @Autowired
    private FoundMapper foundMapper;

    @Autowired
    private WebSocketSessionRegistry webSocketSessionRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 发表评论
     *
     * @param userId
     * @param req
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(Long userId, CommentRequest req) {
        // 查找帖子所有人，作为消息接收人
        Long receiverId = null;
        if ("lost".equals(req.getPostType())) {
            LostItem lostItem = lostMapper.selectById(req.getPostId());
            if (lostItem == null) throw new BusinessException("失物贴不存在");
            receiverId = lostItem.getUserId();
        } else if ("found".equals(req.getPostType())) {
            FoundItem foundItem = foundMapper.selectById(req.getPostId());
            if (foundItem == null) throw new BusinessException("拾物贴不存在");
            receiverId = foundItem.getUserId();
        } else {
            throw new BusinessException("无效的帖子类型");
        }

        Comment comment = new Comment();
        BeanUtils.copyProperties(req, comment);
        comment.setSenderId(userId);
        comment.setReceiverId(receiverId);
        comment.setStatus(IsRead.UNREAD); // 0 为未读
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.insert(comment);

        //发送实时消息进行评论预览
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "newComment");
        payload.put("postId", req.getPostId());
        payload.put("postType", req.getPostType());
        String preview = req.getContent() == null ? "" : req.getContent();
        //预览不能大于80字
        if (preview.length() > 80) {
            preview = preview.substring(0, 80) + "...";
        }
        payload.put("preview", preview);
        try {
            //转为json
            webSocketSessionRegistry.sendText(receiverId, objectMapper.writeValueAsString(payload));
        } catch (Exception ignored) {
        }
    }

    /**
     * 获取帖子的评论
     *
     * @param postId
     * @param postType
     * @return
     */
    @Override
    public List<Comment> getComments(Long postId, String postType) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
                .eq(Comment::getPostType, postType)
                .orderByDesc(Comment::getCreateTime);
        return commentMapper.selectList(wrapper);
    }

    /**
     * 获取未读消息数量
     *
     * @param userId
     * @return
     */
    @Override
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getReceiverId, userId)
                .eq(Comment::getStatus, IsRead.UNREAD); // 0 为未读
        return commentMapper.selectCount(wrapper);
    }

    /**
     * 获取我的消息（评论）
     *
     * @param userId
     * @return
     */
    @Override
    public List<Comment> getMyMessages(Long userId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getReceiverId, userId)
                .orderByDesc(Comment::getCreateTime);
        return commentMapper.selectList(wrapper);
    }

    /**
     * 标记消息为已读
     *
     * @param commentId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long commentId) {
        LambdaUpdateWrapper<Comment> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Comment::getId, commentId)
                .set(Comment::getStatus, IsRead.READ); // 1 为已读
        commentMapper.update(null, wrapper);
    }
}
