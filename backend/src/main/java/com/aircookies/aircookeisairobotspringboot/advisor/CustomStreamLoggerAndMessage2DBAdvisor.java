package com.aircookies.aircookeisairobotspringboot.advisor;

import com.aircookies.aircookeisairobotspringboot.domain.dos.ChatMessageDO;
import com.aircookies.aircookeisairobotspringboot.domain.mapper.ChatMessageMapper;
import com.aircookies.aircookeisairobotspringboot.model.vo.chat.AiChatReqVO;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class CustomStreamLoggerAndMessage2DBAdvisor implements StreamAdvisor {

    private final ChatMessageMapper chatMessageMapper;
    private final AiChatReqVO aiChatReqVO;
    private final TransactionTemplate transactionTemplate;

    public CustomStreamLoggerAndMessage2DBAdvisor(ChatMessageMapper chatMessageMapper, AiChatReqVO aiChatReqVO, TransactionTemplate transactionTemplate) {
        this.chatMessageMapper = chatMessageMapper;
        this.aiChatReqVO = aiChatReqVO;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public int getOrder() {
        return 99; // order值越小越先执行
    }

    public @NonNull String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public @NonNull Flux<ChatClientResponse> adviseStream(@NonNull ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {

        Flux<ChatClientResponse> chatClientResponseFlux = streamAdvisorChain.nextStream(chatClientRequest);

        // 创建 AI 流式推理过程聚合容器（线程安全）
        AtomicReference<StringBuilder> fullReasoning = new AtomicReference<>(new StringBuilder());

        // 创建 AI 流式回答聚合容器（线程安全）
        AtomicReference<StringBuilder> fullContent = new AtomicReference<>(new StringBuilder());

        // 对话uuid
        String chatUuid = aiChatReqVO.getChatId();
        // 用户信息
        String userMessage = aiChatReqVO.getMessage();

        // 返回处理后的流
        return chatClientResponseFlux
                .doOnNext(chatClientResponse -> {
                    ChatResponse chatResponse = chatClientResponse.chatResponse();

                    if (Objects.nonNull(chatResponse) && Objects.nonNull(chatResponse.getResult())) {

                        // 获取 AI 回复的消息
                        AssistantMessage message = Objects.requireNonNull(chatClientResponse.chatResponse().getResult()).getOutput();

                        // 获取推理内容（如果存在）
                        String reasoningChunk = null;
                        if (message instanceof DeepSeekAssistantMessage deepSeekMessage) {
                            reasoningChunk = deepSeekMessage.getReasoningContent();
                        }

//                    log.info("metadata:{}", message.getMetadata());

                        // 逐块收集正式回答
                        String chunk = message.getText();

                        if (reasoningChunk != null && !reasoningChunk.isEmpty()) {
                            log.info("## reasoning chunk: {}", reasoningChunk);
                            fullReasoning.get().append(reasoningChunk);
                        }

                        // 若 chunk 块不为空，则追加到 fullContent 中
                        if (chunk != null && !chunk.isEmpty()) {
                            log.info("## chunk: {}", chunk);
                            fullContent.get().append(chunk);
                        }
                    }
                })
                .doOnComplete(() -> {
                    // 流完成后打印完整推理过程
                    String completeReasoning = fullReasoning.get().toString();
                    log.info("\n=============== FULL AL REASONING ================\n{}\n================================================",
                            completeReasoning);

                    // 流完成后打印完整内容
                    String completeResponse = fullContent.get().toString();
                    log.info("\n=============== FULL AL RESPONSE ================\n{}\n================================================",
                            completeResponse);

                    // 开启编程式事务
                    transactionTemplate.execute(status -> {
                        try {
                            // 1. 存储用户信息
                            chatMessageMapper.insert(ChatMessageDO.builder()
                                    .chatUuid(chatUuid)
                                    .content(userMessage)
                                    .role(MessageType.USER.getValue()) // 用户角色
                                    .createTime(LocalDateTime.now())
                                    .build());
                            // 2. 存储AI回答
                            chatMessageMapper.insert(ChatMessageDO.builder()
                                    .chatUuid(chatUuid)
                                    .content(completeResponse)
                                    .reasoningContent(completeReasoning) // 推理内容
                                    .role(MessageType.ASSISTANT.getValue()) // AI角色
                                    .createTime(LocalDateTime.now())
                                    .build());

                            return true;
                        } catch (Exception e) {
                            status.setRollbackOnly(); // 标记事务为回滚
                            log.error("## 存储数据时发生错误: ", e);
                        }
                        return false;
                    });
                })
                .doOnError(error -> {
                    // 出错是打印已收集的部分
                    String partialResponse = fullContent.get().toString();
                    log.error("## Stream 流出现错误，已收集回答如下：{}", partialResponse, error);
                });
    }
}
