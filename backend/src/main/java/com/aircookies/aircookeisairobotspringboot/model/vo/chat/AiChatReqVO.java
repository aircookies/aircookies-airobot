package com.aircookies.aircookeisairobotspringboot.model.vo.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiChatReqVO {

    @NotBlank(message = "用户信息不能为空")
    private String message;

    /**
     * 对话ID
     */
    private String chatId;

    /**
     * 联网搜索
     */
    private Boolean networkSearch = false;

    @NotBlank(message = "调用的AI大模型名称不能为空")
    private String modelName;

    /**
     * 温度值，默认为0.7
     */
    private Double temperature = 0.7;
}
