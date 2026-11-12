package com.aircookies.aircookeisairobotspringboot.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiCustomerServiceMdUploadedEvent {

    /**
     * t_ai_customer_service_md_storage 表记录主键 ID
     */
    private Long id;

    /**
     * 存储路径
     */
    private String filePath;

    /**
     * 元数据
     */
    private Map<String, Object> metadatas;
}
