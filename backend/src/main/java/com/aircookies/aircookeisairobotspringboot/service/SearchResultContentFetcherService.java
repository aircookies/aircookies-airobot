package com.aircookies.aircookeisairobotspringboot.service;

import com.aircookies.aircookeisairobotspringboot.model.dto.SearchResultDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface SearchResultContentFetcherService {

    /**
     * 并发批量获取搜索结果页面的内容
     *
     * @param searchResults 搜索结果
     * @param timeout
     * @param unit
     * @return
     */
    CompletableFuture<List<SearchResultDTO>> batchFetch(List<SearchResultDTO> searchResults,
                                                        long timeout,
                                                        TimeUnit unit
                                                     );
}
