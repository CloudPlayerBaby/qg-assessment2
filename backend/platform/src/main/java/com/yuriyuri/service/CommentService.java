package com.yuriyuri.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuriyuri.dto.comment.CommentRequest;
import com.yuriyuri.entity.Comment;

import java.util.List;

public interface CommentService {
    /**
     * 发表评论
     * @param userId
     * @param req
     */
    void addComment(Long userId, CommentRequest req);

    /**
     * 获取帖子的评论
     * @param postId
     * @param postType
     * @return
     */
    List<Comment> getComments(Long postId, String postType);

    /**
     * 获取未读消息数量
     * @param userId
     * @return
     */
    Long getUnreadCount(Long userId);

    /**
     * 获取我的消息（评论）
     * @param userId
     * @return
     */
    List<Comment> getMyMessages(Long userId);

    /**
     * 标记消息为已读
     * @param commentId
     */
    void markAsRead(Long commentId);
}
