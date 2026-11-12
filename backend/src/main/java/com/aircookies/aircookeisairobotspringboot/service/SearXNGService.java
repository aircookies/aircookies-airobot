package com.aircookies.aircookeisairobotspringboot.service;

import com.aircookies.aircookeisairobotspringboot.model.dto.SearchResultDTO;

import java.util.List;

public interface SearXNGService {

    /**
     * 调用SearXNG接口进行搜索
     * @param query 搜索关键字
     * @return 搜索结果
     */
    List<SearchResultDTO> search(String query);
}
