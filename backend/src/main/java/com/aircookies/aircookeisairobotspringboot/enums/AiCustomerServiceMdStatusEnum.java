package com.aircookies.aircookeisairobotspringboot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AiCustomerServiceMdStatusEnum {

    PENDING(0, "待处理"),
    VECTORIZING(1, "向量化中"),
    COMPLETED(2, "已完成"),
    FAILED(3, "失败");

    /**
     * 根据 code 获取枚举
     * @param code
     * @return
     */
    public static AiCustomerServiceMdStatusEnum codeOf(Integer code) {
        if (code != null) {
            for (AiCustomerServiceMdStatusEnum value : values()) {
                if (code.equals(value.code)) {
                    return value;
                }
            }
        }

        return null;
    }

    private Integer code;
    private String description;
}
