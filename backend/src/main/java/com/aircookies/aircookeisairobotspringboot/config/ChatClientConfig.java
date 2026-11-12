package com.aircookies.aircookeisairobotspringboot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
    /**
     * 初始化ChatClient客户端
     *
     * @param chatModel 模型
     * @return
     */
    @Bean
    public ChatClient deepseekChatClient(DeepSeekChatModel chatModel) {
        return ChatClient.builder(chatModel)
//                .defaultSystem("请你扮演一名SpringAI项目实战的客服人员")
                .defaultAdvisors(new SimpleLoggerAdvisor()  // 添加SpringAI内置的日志记录功能
                )
                .build();
    }

    @Bean
    public ChatClient QwenChatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor()
                )
                .build();
    }
}
