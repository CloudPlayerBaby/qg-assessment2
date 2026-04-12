package com.yuriyuri.controller;

import com.yuriyuri.common.Result;
import com.yuriyuri.dto.comment.CommentRequest;
import com.yuriyuri.entity.Comment;
import com.yuriyuri.service.CommentService;
import com.yuriyuri.util.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "评论相关",description = "添加评论、获取评论等接口")
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/add")
    @Operation(summary = "添加评论")
    public Result<String> addComment(@RequestBody CommentRequest req) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get("id")).longValue();
        commentService.addComment(userId, req);
        return Result.success("评论成功");
    }

    @GetMapping("/get")
    @Operation(summary = "获取评论")
    public Result<List<Comment>> getComments(@RequestParam Long postId, @RequestParam String postType) {
        return Result.success(commentService.getComments(postId, postType));
    }

    @GetMapping("/unreadCount")
    @Operation(summary = "获取未读消息数")
    public Result<Long> getUnreadCount() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get("id")).longValue();
        return Result.success(commentService.getUnreadCount(userId));
    }

    @GetMapping("/myMessages")
    @Operation(summary = "获取我的消息")
    public Result<List<Comment>> getMyMessages() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get("id")).longValue();
        return Result.success(commentService.getMyMessages(userId));
    }

    /**
     * 标记消息为已读
     * @param commentId
     * @return
     */
    @PostMapping("/read")
    @Operation(summary = "标记消息为已读")
    public Result<String> markAsRead(@RequestParam Long commentId) {
        commentService.markAsRead(commentId);
        return Result.success("标记成功");
    }
}
