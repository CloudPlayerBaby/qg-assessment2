package com.yuriyuri.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient multiModalChatClient(ChatClient.Builder builder) {
        return builder
                .defaultOptions(DashScopeChatOptions.builder()
                        .withModel("qwen-plus")
                        .build())
                .build();
    }
}
