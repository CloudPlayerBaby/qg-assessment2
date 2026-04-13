package com.yuriyuri.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.yuriyuri.dto.ai.AiSuggestionRequest;
import com.yuriyuri.entity.AiSuggestion;
import com.yuriyuri.mapper.AiMapper;
import com.yuriyuri.service.AiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {
    @Autowired
    private AiMapper aiMapper;

    private final ChatClient chatClient;

    public AiServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Flux<String> polishDescription(Long userId, AiSuggestionRequest req) {
        AiSuggestion aiSuggestion = new AiSuggestion();
        aiSuggestion.setUserId(userId);
        aiSuggestion.setDescription(req.getDescription());
        aiMapper.insert(aiSuggestion);

        StringBuilder fullSuggestion = new StringBuilder();

        return chatClient.prompt()
                .system("你是一个专业的失物招领助手。请将用户提供的物品描述润色得更加客观、简洁、清晰，并突出关键特征。" +
                        "每次润色时，请用不同的表达方式和侧重点来描述同一个物品，让每次的结果都有所不同。" +
                        "例如：当用户只填写 物品名称：校园卡，你可以自动补充描述：“该物品为校园卡，可能用于校园身份认证或消费，请尽快联系失主。”")
                .user(req.getDescription())
                .options(DashScopeChatOptions.builder()
                        .withTemperature(1.8)   // 增加随机性
                        .withTopP(0.9)          // 增加词汇多样性
                        .build())
                .stream()
                .content()
                //每当收到一个文本块，就追加到 StringBuilder 中，逐步构建完整内容
                .doOnNext(fullSuggestion::append)
                .doOnComplete(() -> {
                    //完成后插入到数据库
                    aiSuggestion.setSuggestion(fullSuggestion.toString());
                    aiMapper.updateById(aiSuggestion);
                });
    }

    @Override
    public String chat(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    @Override
    public String generateAnalysisReport(List<Map<String, Object>> lostPlaceStats,
                                      List<Map<String, Object>> lostItemStats,
                                      List<Map<String, Object>> foundPlaceStats,
                                      List<Map<String, Object>> foundItemStats) {
        return chatClient.prompt()
                .system("你是一个专业的失物招领数据分析专家。你会收到4个数据列表：\n" +
                        "1. lostPlaceStats: 失物地点统计，每个元素包含location（地点）和count（数量）\n" +
                        "2. lostItemStats: 失物物品统计，每个元素包含item（物品名）和count（数量）\n" +
                        "3. foundPlaceStats: 拾物地点统计，每个元素包含location（地点）和count（数量）\n" +
                        "4. foundItemStats: 拾物物品统计，每个元素包含item（物品名）和count（数量）\n\n" +
                        "请根据这些数据生成一份简洁明了的分析报告，指出：\n" +
                        "1. 哪个区域的失物/拾物最多\n" +
                        "2. 哪种物品最近丢失/拾取较多\n" +
                        "3. 给出一些简短的建议\n" +
                        "请用简洁、友好的语言回答，控制在300字以内。你的回答里不需要追加其他问题和建议，例如：“需要可视化图表或导出Excel，随时告诉我～”\n" +
                        "**、# 等md语法也不需要，纯文本+emoji/颜文字即可 ")
                .user("失物地点统计：" + lostPlaceStats + "\n\n" +
                        "失物物品统计：" + lostItemStats + "\n\n" +
                        "拾物地点统计：" + foundPlaceStats + "\n\n" +
                        "拾物物品统计：" + foundItemStats)
                .options(DashScopeChatOptions.builder()
                        .withTemperature(0.7)
                        .build())
                .call()
                .content();
    }
}
