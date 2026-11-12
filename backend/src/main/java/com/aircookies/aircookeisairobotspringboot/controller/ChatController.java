package com.aircookies.aircookeisairobotspringboot.controller;

import com.aircookies.aircookeisairobotspringboot.advisor.CustomChatMemoryAdvisor;
import com.aircookies.aircookeisairobotspringboot.advisor.CustomStreamLoggerAndMessage2DBAdvisor;
import com.aircookies.aircookeisairobotspringboot.advisor.NetworkSearchAdvisor;
import com.aircookies.aircookeisairobotspringboot.aspect.ApiOperationLog;
import com.aircookies.aircookeisairobotspringboot.domain.mapper.ChatMessageMapper;
import com.aircookies.aircookeisairobotspringboot.model.vo.chat.*;
import com.aircookies.aircookeisairobotspringboot.service.ChatService;
import com.aircookies.aircookeisairobotspringboot.service.SearXNGService;
import com.aircookies.aircookeisairobotspringboot.service.SearchResultContentFetcherService;
import com.aircookies.aircookeisairobotspringboot.utils.PageResponse;
import com.aircookies.aircookeisairobotspringboot.utils.Response;
import com.google.common.collect.Lists;
import com.openai.models.beta.assistants.Assistant;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/ai/chat")
@Slf4j
public class ChatController {

    @Resource
    private ChatService chatService;

    @Resource
    private ChatMessageMapper chatMessageMapper;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private SearXNGService searXNGService;
    @Resource
    private SearchResultContentFetcherService searchResultContentFetcherService;

    @Value("${spring.ai.deepseek.base-url}")
    private String baseUrl;
    @Value("${spring.ai.deepseek.api-key}")
    private String apiKey;

    private ChatModel chatModel;

    /**
     * 初始化ChatClient：baseUrl/apiKey属于连接配置，构建一次即可复用；
     * 模型名称和温度值属于请求级参数，每次请求动态覆盖，无需重建客户端
     */
    @PostConstruct
    public void initChatClient() {
        this.chatModel = DeepSeekChatModel.builder()
                .deepSeekApi(DeepSeekApi.builder()
                        .baseUrl(baseUrl)
                        .apiKey(apiKey)
                        .build())
                .build();
    }

    @PostMapping("/new")
    @ApiOperationLog(description = "新建对话")
    public Response<?> newChat(@RequestBody @Validated NewChatReqVO newChatReqVO) {
        return chatService.newChat(newChatReqVO);
    }

    /**
     * 流式对话
     *
     * @return
     */
    @PostMapping(value = "/completion", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiOperationLog(description = "流式对话")
    public Flux<AIResponse> chat(@RequestBody @Validated AiChatReqVO aiChatReqVO) {
        // 用户消息
        String userMessage = aiChatReqVO.getMessage();
        // 模型名称
        String modelName = aiChatReqVO.getModelName();
        // 温度值
        Double temperature = aiChatReqVO.getTemperature();

        ChatClient chatClient = ChatClient.builder(chatModel).build();

        // 是否开启联网搜索
        boolean networkSearch = aiChatReqVO.getNetworkSearch();

        // Advisor集合
        List<Advisor> advisors = Lists.newArrayList();

        if (networkSearch) {
            // 添加联网搜索advisor
            advisors.add(new NetworkSearchAdvisor(searXNGService, searchResultContentFetcherService));
        } else {
            // 添加自定义对话记忆advisor
            advisors.add(new CustomChatMemoryAdvisor(chatMessageMapper, aiChatReqVO, 50));
        }

        // 添加自定义打印流式对话日志advisor
        advisors.add(new CustomStreamLoggerAndMessage2DBAdvisor(chatMessageMapper, aiChatReqVO, transactionTemplate));

        // 构建提示词并流式返回,每次请求动态设置模型名称和温度值
        return chatClient.prompt()
                .user(userMessage)
                .options(DeepSeekChatOptions.builder()
                        .model(modelName)
                        .temperature(temperature)
                )
                .advisors(advisors)
                .stream()
                .chatResponse()
                .mapNotNull(chatResponse -> {
                    if (Objects.nonNull(chatResponse) && Objects.nonNull(chatResponse.getResult())) {
                        // 获取流式返回的响应内容
                        AssistantMessage message = chatResponse.getResult().getOutput();

                        // 获取正式回答
                        String text = message.getText();

                        // 获取推理内容(若存在)
                        String reasoningContent = null;
                        if (message instanceof DeepSeekAssistantMessage deepSeekMessage) {
                            reasoningContent = deepSeekMessage.getReasoningContent();
                        }


                        // 构建响应
                        if (StringUtils.isNotBlank(reasoningContent)) {
                            // 返回思考过程
                            return AIResponse.builder().reasoning(reasoningContent).build();
                        }

                        return AIResponse.builder().v(text).build();

                    }

                    return null;
                })
                .doOnCancel(() -> log.info("客户端断开连接, chatId: {}", aiChatReqVO.getChatId()));
    }

    @PostMapping("/message/list")
    @ApiOperationLog(description = "查询消息记录")
    public PageResponse<FindChatHistoryMessagePageListRspVO> findChatHistoryMessagePageListRspVOPageResponse(@RequestBody @Validated FindChatHistoryMessagePageListReqVO findChatHistoryMessagePageListReqVO) {
        return chatService.findChatHistoryMessagePageList(findChatHistoryMessagePageListReqVO);
    }

    @PostMapping("/list")
    @ApiOperationLog(description = "查询历史对话")
    public PageResponse<FindChatHistoryPageListRspVO> findChatHistoryPageList(@RequestBody @Validated FindChatHistoryPageListReqVO findChatHistoryPageListReqVO) {
        return chatService.findChatHistoryPageList(findChatHistoryPageListReqVO);
    }

    @PostMapping("/summary/rename")
    @ApiOperationLog(description = "重命名对话摘要")
    public Response<?> renameChatSummary(@RequestBody @Validated RenameChatReqVO renameChatReqVO) {
        return chatService.renameChatSummary(renameChatReqVO);
    }

    @PostMapping("/delete")
    @ApiOperationLog(description = "删除对话")
    public Response<?> deleteChat(@RequestBody @Validated DeleteChatReqVO deleteChatReqVO) {
        return chatService.deleteChat(deleteChatReqVO);
    }
}
