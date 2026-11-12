package com.aircookies.aircookeisairobotspringboot.controller;

import com.aircookies.aircookeisairobotspringboot.advisor.CustomerServiceAdvisor;
import com.aircookies.aircookeisairobotspringboot.aspect.ApiOperationLog;
import com.aircookies.aircookeisairobotspringboot.model.vo.chat.AIResponse;
import com.aircookies.aircookeisairobotspringboot.model.vo.customerService.*;
import com.aircookies.aircookeisairobotspringboot.service.CustomerService;
import com.aircookies.aircookeisairobotspringboot.utils.PageResponse;
import com.aircookies.aircookeisairobotspringboot.utils.Response;
import com.google.common.collect.Lists;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/customer-service")
@Slf4j
public class AiCustomerServiceController {

    @Resource
    private CustomerService customerService;
    @Resource
    private VectorStore vectorStore;

    private ChatModel chatModel;

    @Value("${customer-service.model}")
    private String model;
    @Value("${customer-service.temperature}")
    private Double temperature;
    @Value("${spring.ai.deepseek.api-key}")
    private String apiKey;
    @Value("${spring.ai.deepseek.base-url}")
    private String baseUrl;

    @PostConstruct
    public void initModel() {
        // 构建 ChatModel
        this.chatModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .model(model)
                        .temperature(temperature)
                        .baseUrl(baseUrl)
                        .apiKey(apiKey)
                        .build())
                .build();
    }

    /**
     * 问答MD文件上传
     *
     * @param file
     * @return
     */
    @PostMapping(value = "/md/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<?> uploadMarkdownFile(@RequestPart(value = "file", required = false) MultipartFile file) {
        return customerService.uploadMarkdownFile(file);
    }

    @PostMapping("/md/delete")
    @ApiOperationLog(description = "删除 Markdown 问答文件")
    public Response<?> deleteMarkdownFile(@RequestBody @Validated DeleteMarkdownFileReqVO deleteMarkdownFileReqVO) {
        return customerService.deleteMarkdownFile(deleteMarkdownFileReqVO);
    }

    @PostMapping("/md/list")
    @ApiOperationLog(description = "Markdown 问答文件分页查询")
    public PageResponse<FindMarkdownFilePageListRspVO> findMarkdownFilePageList(@RequestBody @Validated FindMarkdownFilePageListReqVO findMarkdownFilePageListReqVO) {
        return customerService.findMarkdownFilePageList(findMarkdownFilePageListReqVO);
    }

    @PostMapping("/md/update")
    @ApiOperationLog(description = "修改 Markdown 问答文件信息")
    public Response<?> updateMarkdownFile(@RequestBody @Validated UpdateMarkdownFileReqVO updateMarkdownFileReqVO) {
        return customerService.updateMarkdownFile(updateMarkdownFileReqVO);
    }

    /**
     * 流式对话
     * @return
     */
    @PostMapping(value = "/chat/completion", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiOperationLog(description = "AI 智能客服对话")
    public Flux<AIResponse> chat(@RequestBody @Validated AiCustomerServiceChatReqVO aiChatReqVO) {
        // 用户消息
        String userMessage = aiChatReqVO.getMessage();

        ChatClient.ChatClientRequestSpec chatClientRequestSpec = ChatClient.create(chatModel)
                .prompt()
                .user(userMessage);

        // Advisor 集合
        List<Advisor> advisors = Lists.newArrayList();
        advisors.add(new CustomerServiceAdvisor(vectorStore)); // 检索向量库，组合增强提示词

        // 应用 Advisor 集合
        chatClientRequestSpec.advisors(advisors);

        // 流式输出
        return chatClientRequestSpec
                .stream()
                .content()
                .map(text -> AIResponse.builder().v(text).build())
                .doOnCancel(() -> log.info("客户端断开连接"));
    }
}
