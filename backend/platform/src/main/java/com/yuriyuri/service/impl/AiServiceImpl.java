package com.yuriyuri.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuriyuri.common.BusinessException;
import com.yuriyuri.constant.ai.AiPrompts;
import com.yuriyuri.dto.ai.AiSuggestionRequest;
import com.yuriyuri.entity.AiSuggestion;
import com.yuriyuri.entity.FoundItem;
import com.yuriyuri.entity.LostItem;
import com.yuriyuri.mapper.AiMapper;
import com.yuriyuri.service.AiService;
import com.yuriyuri.service.tool.FoundItemSearchTool;
import com.yuriyuri.service.tool.LostItemSearchTool;
import com.yuriyuri.util.AiContextUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {
    @Autowired
    private AiMapper aiMapper;

    @Autowired
    private FoundItemSearchTool foundItemSearchTool;

    @Autowired
    private LostItemSearchTool lostItemSearchTool;

    private final ChatClient chatClient;

    //使用多模态模型，可识别图片
    public AiServiceImpl(@Qualifier("multiModalChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * ai润色描述（包含图片识别）
     *
     * @param userId
     * @param req
     * @return
     */
    @Override
    public Flux<String> polishDescription(Long userId, AiSuggestionRequest req) {
        AiSuggestion aiSuggestion = new AiSuggestion();
        aiSuggestion.setUserId(userId);
        aiSuggestion.setDescription(req.getDescription());
        aiMapper.insert(aiSuggestion);

        //拼接生成的建议
        StringBuilder fullSuggestion = new StringBuilder();

        String userPrompt;
        String systemPrompt;
        String type = req.getType();

        // 选择对应场景的 system prompt
        if (req.getImageUrl() != null && !req.getImageUrl().trim().isEmpty()) {
            systemPrompt = AiPrompts.POLISH_WITH_IMAGE;
        } else if ("lost".equals(type)) {
            systemPrompt = AiPrompts.POLISH_LOST;
        } else {
            systemPrompt = AiPrompts.POLISH_FOUND;
        }

        // 如果有图片
        if (req.getImageUrl() != null && !req.getImageUrl().trim().isEmpty()) {
            userPrompt = "物品描述：" + req.getDescription();

            URL imageUrl;
            try {
                imageUrl = new URL(req.getImageUrl().trim());
            } catch (MalformedURLException e) { //地址异常
                throw new BusinessException("图片地址格式不正确");
            }
            //生成图片格式（如.jpeg）
            MimeType mimeType = getImageMimeType(req.getImageUrl().trim());

            // qwen-vl-plus 在当前依赖版本下流式会偶发空指针，这里只能改成非流式调用了
            String content = chatClient.prompt()
                    .system(systemPrompt)
                    .user(u -> u
                            .text(userPrompt)
                            //利用.media传入url
                            .media(mimeType, imageUrl))
                    .options(DashScopeChatOptions.builder()
                            //使用多模态识别图片
                            .withModel("qwen-vl-plus")
                            .withMultiModel(true)
                            .build())
                    .call()
                    .content();
            //防空指针
            if (content == null) {
                content = "";
            }
            aiSuggestion.setSuggestion(content);
            aiMapper.updateById(aiSuggestion);
            //这个版本没办法，识图时只能call了
            return Flux.just(content);
        } else {
            //如果没有图片，则不进行任何处理
            userPrompt = req.getDescription();
        }

        //无图片的情况
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .options(DashScopeChatOptions.builder()
                        .withModel("qwen-plus")
                        .build())
                .stream()
                .content()
                //在stream流式输出下需要拼接
                .doOnNext(fullSuggestion::append)
                .doOnComplete(() -> {
                    aiSuggestion.setSuggestion(fullSuggestion.toString());
                    aiMapper.updateById(aiSuggestion);
                });
    }

    /**
     * ai生成描述
     *
     * @param lostPlaceStats
     * @param lostItemStats
     * @param foundPlaceStats
     * @param foundItemStats
     * @return
     */
    @Override
    public String generateAnalysisReport(List<Map<String, Object>> lostPlaceStats,
                                         List<Map<String, Object>> lostItemStats,
                                         List<Map<String, Object>> foundPlaceStats,
                                         List<Map<String, Object>> foundItemStats) {
        // 格式化统计数据，减少上下文长度
        String userPrompt = String.format("%s\n%s\n%s\n%s",
                AiContextUtil.formatStatistics("失物地点", lostPlaceStats),
                AiContextUtil.formatStatistics("失物物品", lostItemStats),
                AiContextUtil.formatStatistics("拾物地点", foundPlaceStats),
                AiContextUtil.formatStatistics("拾物物品", foundItemStats)
        );

        return chatClient.prompt()
                .system(AiPrompts.ANALYSIS_REPORT)
                .user(userPrompt)
                .call()
                .content();
    }

    /**
     * 失物请求的ai查询（查拾物）
     *
     * @param userId
     * @param description
     * @return
     */
    @Override
    public Flux<String> FoundItemSearch(Long userId, String description) {
        // 插入AI使用记录
        AiSuggestion aiSuggestion = new AiSuggestion();
        aiSuggestion.setUserId(userId);
        aiSuggestion.setDescription(description);
        aiMapper.insert(aiSuggestion);

        // 获取并格式化数据，限制上下文长度
        List<FoundItem> foundItems = foundItemSearchTool.getAllFoundItem();
        String context = AiContextUtil.formatFoundItems(foundItems);

        StringBuilder fullSuggestion = new StringBuilder();

        return chatClient.prompt()
                .system(AiPrompts.SEARCH_FOUND)
                .user(String.format("丢失物品：%s\n\n拾物记录：\n%s", description, context))
                .options(DashScopeChatOptions.builder()
                        .withModel("qwen-plus")
                        .build())
                .stream()
                .content()
                .doOnNext(fullSuggestion::append)
                .doOnComplete(() -> {
                    aiSuggestion.setSuggestion(fullSuggestion.toString());
                    aiMapper.updateById(aiSuggestion);
                });
    }

    /**
     * 拾物请求的ai查询（查失物）
     *
     * @param userId
     * @param description
     * @return
     */
    @Override
    public Flux<String> LostItemSearch(Long userId, String description) {
        // 插入AI使用记录
        AiSuggestion aiSuggestion = new AiSuggestion();
        aiSuggestion.setUserId(userId);
        aiSuggestion.setDescription(description);
        aiMapper.insert(aiSuggestion);

        // 获取并格式化数据，限制上下文长度
        List<LostItem> lostItems = lostItemSearchTool.getAllLostItem();
        String context = AiContextUtil.formatLostItems(lostItems);

        StringBuilder fullSuggestion = new StringBuilder();

        return chatClient.prompt()
                .system(AiPrompts.SEARCH_LOST)
                .user(String.format("捡到物品：%s\n\n失物记录：\n%s", description, context))
                .options(DashScopeChatOptions.builder()
                        .withModel("qwen-plus")
                        .build())
                .stream()
                .content()
                .doOnNext(fullSuggestion::append)
                .doOnComplete(() -> {
                    aiSuggestion.setSuggestion(fullSuggestion.toString());
                    aiMapper.updateById(aiSuggestion);
                });
    }

    /**
     * 限制用户一天的用量
     * @param userId
     * @return
     */
    @Override
    public Boolean limitAiUse(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();         // 今日 00:00:00
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);   // 今日 23:59:59.999999999

        LambdaQueryWrapper<AiSuggestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiSuggestion::getUserId, userId)
                .between(AiSuggestion::getCreateTime, startOfDay, endOfDay);
        Long count = aiMapper.selectCount(wrapper);
        return count < 20;
    }

    //以下为方法

    //检测图片格式的方法
    public MimeType getImageMimeType(String imageUrl) {
        String lower = imageUrl.toLowerCase();
        if (lower.endsWith(".png")) {
            return MimeTypeUtils.parseMimeType("image/png");
        }
        if (lower.endsWith(".webp")) {
            return MimeTypeUtils.parseMimeType("image/webp");
        }
        if (lower.endsWith(".gif")) {
            return MimeTypeUtils.parseMimeType("image/gif");
        }
        return MimeTypeUtils.parseMimeType("image/jpeg");
    }
}
