package com.yuriyuri.controller;

import com.yuriyuri.common.Result;
import com.yuriyuri.dto.message.PrivateChatRow;
import com.yuriyuri.dto.message.PrivateMessageSendRequest;
import com.yuriyuri.entity.PrivateMessage;
import com.yuriyuri.service.PrivateMessageService;
import com.yuriyuri.util.ThreadLocalUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/privateMessage")
public class PrivateMessageController {

    @Autowired
    private PrivateMessageService privateMessageService;

    @PostMapping("/send")
    public Result<String> send(@Valid @RequestBody PrivateMessageSendRequest req) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = ((Number) map.get("id")).longValue();
        privateMessageService.send(userId, req);
        return Result.success("发送成功");
    }

    @GetMapping("/list")
    public Result<List<PrivateMessage>> list(
            @RequestParam Long postId,
            @RequestParam String postType,
            @RequestParam Long peerId) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = ((Number) map.get("id")).longValue();
        return Result.success(privateMessageService.list(userId, postId, postType, peerId));
    }

    @PostMapping("/read")
    public Result<String> read(
            @RequestParam Long postId,
            @RequestParam String postType,
            @RequestParam Long peerId) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = ((Number) map.get("id")).longValue();
        privateMessageService.markRead(userId, postId, postType, peerId);
        return Result.success("已读");
    }

    @GetMapping("/unreadCount")
    public Result<Long> unreadCount() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = ((Number) map.get("id")).longValue();
        return Result.success(privateMessageService.countUnread(userId));
    }

    @GetMapping("/conversations")
    public Result<List<PrivateChatRow>> conversations() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = ((Number) map.get("id")).longValue();
        return Result.success(privateMessageService.myChats(userId));
    }
}
