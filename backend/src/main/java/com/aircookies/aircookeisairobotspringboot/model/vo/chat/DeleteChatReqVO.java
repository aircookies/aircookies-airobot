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
public class DeleteChatReqVO {

    @NotBlank(message = "对话UUID不能为空")
    private String uuid;
}
