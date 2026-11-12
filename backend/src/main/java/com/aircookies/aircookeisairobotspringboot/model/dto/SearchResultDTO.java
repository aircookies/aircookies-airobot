package com.aircookies.aircookeisairobotspringboot.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchResultDTO {

    /**
     * 页面访问链接
     */
    private String url;

    /**
     * 相关性评分
     */
    private Double score;

    /**
     * 页面内容
     */
    private String content;
}
