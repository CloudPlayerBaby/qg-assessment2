package com.yuriyuri.controller;

import com.yuriyuri.common.Result;
import com.yuriyuri.dto.message.PrivateMessageVO;
import com.yuriyuri.dto.message.PrivateSessionRequest;
import com.yuriyuri.dto.message.PrivateSessionSummaryVO;
import com.yuriyuri.dto.message.PrivateSessionVO;
import com.yuriyuri.service.PrivateMessageService;
import com.yuriyuri.util.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "私信相关",description = "获取会话、会话历史等接口")
@RestController
@RequestMapping("/privateMessage")
public class PrivateMessageController {

    @Autowired
    private PrivateMessageService privateMessageService;

    @PostMapping("/session")
    public Result<PrivateSessionVO> ensureSession(@Valid @RequestBody PrivateSessionRequest req) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get("id")).longValue();
        return Result.success(privateMessageService.ensureSession(userId, req));
    }

    @GetMapping("/history")
    public Result<List<PrivateMessageVO>> history(@RequestParam String sessionId) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get("id")).longValue();
        return Result.success(privateMessageService.listHistory(userId, sessionId));
    }

    @PostMapping("/read")
    public Result<String> markRead(@RequestParam String sessionId) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get("id")).longValue();
        privateMessageService.markSessionRead(userId, sessionId);
        return Result.success("已标记已读");
    }

    @GetMapping("/unreadCount")
    @Operation(summary = "获取未读数量")
    public Result<Long> unreadCount() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get("id")).longValue();
        return Result.success(privateMessageService.countUnread(userId));
    }

    @GetMapping("/sessions")
    public Result<List<PrivateSessionSummaryVO>> sessions() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get("id")).longValue();
        return Result.success(privateMessageService.listSessionSummaries(userId));
    }
}
