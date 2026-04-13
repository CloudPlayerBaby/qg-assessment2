package com.yuriyuri.controller;

import com.yuriyuri.common.Result;
import com.yuriyuri.dto.ai.AiSuggestionRequest;
import com.yuriyuri.service.AiService;
import com.yuriyuri.util.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;


@RestController
@RequestMapping("/ai")
@Tag(name = "ai相关",description = "润色物品、总结数据等接口")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping(value = "/polish", produces = "text/event-stream")
    @Operation(summary = "ai润色物品描述")
    public Flux<String> polish(@Valid @RequestBody AiSuggestionRequest req) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get("id")).longValue();
        return aiService.polishDescription(userId,req);
    }

    @GetMapping("/chat")
    public Result<String> chat(@RequestParam String message) {
        return Result.success(aiService.chat(message));
    }
}
