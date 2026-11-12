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
public class NewChatReqVO {

    @NotBlank(message = "用户信息不能为空")
    private String message;
}
