package com.yuriyuri.controller;

import com.yuriyuri.annotation.RequireIdentity;
import com.yuriyuri.common.Result;
import com.yuriyuri.dto.ai.AiSearchRequest;
import com.yuriyuri.dto.ai.AiSuggestionRequest;
import com.yuriyuri.service.AiService;
import com.yuriyuri.util.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.util.Map;


@RestController
@RequestMapping("/ai")
@Tag(name = "ai相关",description = "润色物品、总结数据等接口")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping(value = "/polish", produces = "text/event-stream")
    @Operation(summary = "ai润色物品描述")
    public Flux<String> polish(@Valid @RequestBody AiSuggestionRequest req) throws MalformedURLException {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get("id")).longValue();
        return aiService.polishDescription(userId,req);
    }

    @PostMapping("/search/found")
    @Operation(summary = "AI搜索拾物（发布失物时调用）")
    public Flux<String> searchFound(@Valid @RequestBody AiSearchRequest req) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get("id")).longValue();
        return aiService.FoundItemSearch(userId, req.getDescription());
    }

    @PostMapping("/search/lost")
    @Operation(summary = "AI搜索失物（发布拾物时调用）")
    public Flux<String> searchLost(@Valid @RequestBody AiSearchRequest req) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get("id")).longValue();
        return aiService.LostItemSearch(userId, req.getDescription());
    }

    @GetMapping("/limit")
    @RequireIdentity({"user"}) //只有用户有限制
    @Operation(summary = "限制用户一天只能用20次ai")
    public Result<Boolean> limitAiUse(){
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get("id")).longValue();
        Boolean result = aiService.limitAiUse(userId);
        return Result.success(result);
    }
}
