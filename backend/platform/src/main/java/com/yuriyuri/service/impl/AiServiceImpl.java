package com.yuriyuri.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuriyuri.common.BusinessException;
import com.yuriyuri.dto.ai.AiSuggestionRequest;
import com.yuriyuri.entity.AiSuggestion;
import com.yuriyuri.entity.FoundItem;
import com.yuriyuri.entity.LostItem;
import com.yuriyuri.mapper.AiMapper;
import com.yuriyuri.service.AiService;
import com.yuriyuri.service.tool.FoundItemSearchTool;
import com.yuriyuri.service.tool.LostItemSearchTool;
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

        String systemPrompt = "";

        String type = req.getType();

        //如果是失物
        if ("lost".equals(type)) {
            systemPrompt = "你是一个专业的失物招领助手。请将用户提供的物品描述润色得更加客观、简洁、清晰，并突出关键特征。" +
                    "每次润色时，请用不同的表达方式和侧重点来描述同一个物品，让每次的结果都有所不同。" +
                    "例如：当用户只填写 物品名称：校园卡，你可以自动补充描述：\"我丢了一张校园卡，上面有我的学号1234，如有捡到请联系我，谢谢。\"";
        } else if ("found".equals(type)) {
            systemPrompt = "你是一个专业的失物招领助手。请将用户提供的物品描述润色得更加客观、简洁、清晰，并突出关键特征。" +
                    "每次润色时，请用不同的表达方式和侧重点来描述同一个物品，让每次的结果都有所不同。" +
                    "例如：当用户只填写 物品名称：校园卡，你可以自动补充描述：\"该校园卡上已注明了失主信息，姓名：张*三，学号：尾号为1234，如有遗失请联系我\"";
        }


        //如果有图片
        if (req.getImageUrl() != null && !req.getImageUrl().trim().isEmpty()) {
            systemPrompt = "你是一个专业的多模态失物招领助手。请结合用户提供的物品图片和描述，分析图片内容，" +
                    "生成更详细的物品特征描述。请用中文回答，描述要客观、简洁、清晰，并突出关键特征。" +
                    "例如：用户上传了一张图片并描述\"钥匙\"，你可以分析图片生成：\"我丢的钥匙为古铜色，用一条红色的绳子系着，上边贴着\"808\"，是我的的宿舍号，如有捡到请联系我，谢谢\"。" +
                    "用户上传了一张图片并描述\"校园卡\"，你可以分析图片生成：\"校园卡上有我的信息，姓名：张*三，学号：尾号为1234，如有捡到请联系我，谢谢\"。" +
                    "用户上传了一张图片并描述\"耳机\"，你可以分析图片生成：\"耳机为粉色外观，耳机盒子上有一张紫色的贴纸，如有捡到请联系我，谢谢\"。";
            userPrompt = "请结合图片和文字描述润色：\n物品描述：" + req.getDescription();

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

    @Override
    public String chat(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
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
                        .build())
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

        List<FoundItem> foundItems = foundItemSearchTool.getAllFoundItem();

        String systemPrompt = "你是一个专业的失物招领匹配助手。用户丢失了物品，提供了丢失物品的描述。" +
                "你需要根据用户的描述，从数据库中的拾物记录里，找出最可能匹配的记录。" +
                "请按照相关性从大到小排序返回结果，只返回相关度较高的记录（最多5条）。" +
                "每条结果请包含：物品名称、拾取地点、拾取时间、描述、联系方式。" +
                "如果没有找到匹配的记录，请返回\"未找到匹配的拾物记录\"。" +
                "请用简洁、友好的语言回答。" +
                "返回结果保持排版清晰。" +
                "**、# 等md语法也不需要，纯文本+emoji/颜文字即可。" +
                "不需要给用户看分析过程，只需要最终的结果。";

        StringBuilder fullSuggestion = new StringBuilder();

        return chatClient.prompt()
                .system(systemPrompt)
                .user("用户丢失物品描述：" + description + "\n\n数据库中的拾物记录：" + foundItems)
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

        List<LostItem> lostItems = lostItemSearchTool.getAllLostItem();

        String systemPrompt = "你是一个专业的失物招领匹配助手。用户捡到了物品，提供了捡到物品的描述。" +
                "你需要根据用户的描述，从数据库中的失物记录里，找出最可能匹配的记录。" +
                "请按照相关性从大到小排序返回结果，只返回相关度较高的记录（最多5条）。" +
                "每条结果请包含：物品名称、丢失地点、丢失时间、描述。" +
                "如果没有找到匹配的记录，请返回\"未找到匹配的失物记录\"。" +
                "请用简洁、友好的语言回答。" +
                "返回结果保持排版清晰。" +
                "**、# 等md语法也不需要，纯文本+emoji/颜文字即可。" +
                "不需要给用户看分析过程，只需要最终的结果。";

        StringBuilder fullSuggestion = new StringBuilder();

        return chatClient.prompt()
                .system(systemPrompt)
                .user("用户捡到物品描述：" + description + "\n\n数据库中的失物记录：" + lostItems)
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
