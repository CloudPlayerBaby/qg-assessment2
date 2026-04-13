package com.yuriyuri.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuriyuri.dto.comment.CommentRequest;
import com.yuriyuri.entity.Comment;

import java.util.List;

public interface CommentService {
    //流程：添加评论->获取评论->得到未读数->楼主收到消息->点进去消息已读
    void addComment(Long userId, CommentRequest req);
    List<Comment> getComments(Long postId, String postType);
    Long getUnreadCount(Long userId);
    List<Comment> getMyMessages(Long userId);
    void markAsRead(Long commentId);
}
