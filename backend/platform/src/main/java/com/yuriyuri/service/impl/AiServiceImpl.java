package com.yuriyuri.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.yuriyuri.dto.ai.AiSuggestionRequest;
import com.yuriyuri.entity.AiSuggestion;
import com.yuriyuri.mapper.AiMapper;
import com.yuriyuri.service.AiService;
import org.springframework.ai.chat.client.ChatClient;
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

        String userPrompt = req.getDescription();
        String systemPrompt = "你是一个专业的失物招领助手。请将用户提供的物品描述润色得更加客观、简洁、清晰，并突出关键特征。" +
                "每次润色时，请用不同的表达方式和侧重点来描述同一个物品，让每次的结果都有所不同。" +
                "例如：当用户只填写 物品名称：校园卡，你可以自动补充描述：\"该物品为校园卡，可能用于校园身份认证或消费，请尽快联系失主。\"";

        if (req.getImageUrl() != null && !req.getImageUrl().trim().isEmpty()) {
            systemPrompt = "你是一个专业的多模态失物招领助手。请结合用户提供的物品图片和描述，分析图片内容，" +
                    "生成更详细的物品特征描述。请用中文回答，描述要客观、简洁、清晰，并突出关键特征。" +
                    "例如：用户上传了一张图片并描述\"钥匙\"，你可以分析图片生成：\"该钥匙为古铜色，用一条红色的绳子系着，上边贴着\"808\"，可能是失主的宿舍号\"。" +
                    "用户上传了一张图片并描述\"校园卡\"，你可以分析图片生成：\"该校园卡上已注明了失主信息，姓名：张*三，学号：尾号为1234\"。" +
                    "用户上传了一张图片并描述\"耳机\"，你可以分析图片生成：\"该耳机为粉色外观，耳机盒子上有一张紫色的贴纸\"。";
            userPrompt = "物品描述：" + req.getDescription() + "\n图片URL：" + req.getImageUrl();
        }

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .options(DashScopeChatOptions.builder()
                        .withTemperature(1.8)
                        .withTopP(0.9)
                        .build())
                .stream()
                .content()
                .doOnNext(fullSuggestion::append)
                .doOnComplete(() -> {
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
                        "请用简洁、友好的语言回答，控制在300字以内。你的回答里不需要追加其他问题和建议，例如：\"需要可视化图表或导出Excel，随时告诉我～\"\n" +
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
