package com.aircookies.aircookeisairobotspringboot.service.impl;

import com.aircookies.aircookeisairobotspringboot.service.SearchResultContentFetcherService;
import com.aircookies.aircookeisairobotspringboot.model.dto.SearchResultDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchResultContentFetcherServiceImpl implements SearchResultContentFetcherService {
    @Resource
    private OkHttpClient okHttpClient;

    @Resource(name = "httpRequestExecutor")
    private ThreadPoolTaskExecutor httpExecutor;

    @Resource(name = "resultProcessingExecutor")
    private ThreadPoolTaskExecutor processingExecutor;

    /**
     * 批量获取搜索结果内容
     * @param searchResults 搜索结果列表
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return
     */
    @Override
    public CompletableFuture<List<SearchResultDTO>> batchFetch(List<SearchResultDTO> searchResults, long timeout, TimeUnit unit) {
        // 1. 为每个搜索结果创建独立的异步获取任务
        List<CompletableFuture<SearchResultDTO>> futures = searchResults.stream()
                .map(result -> asyncFetchContentForResult(result, timeout, unit))
                .toList();

        // 2. 合并所有独立任务为一个聚合任务
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // 3. 当所有任务完成后收集结果
        return allFutures.thenApplyAsync(v ->  // 所有任务完成后触发
            futures.stream() // 遍历所有已完成的任务
                    .map(future -> { // 提取每个任务的结果
                        SearchResultDTO searchResult = future.join();
                        // 获取页面HTML代码
                        String html = searchResult.getContent();

                        if (StringUtils.isNotBlank(html)) {
                            // 提取HTML中的文本
                            searchResult.setContent(Jsoup.parse(html).text());
                        }

                        return searchResult;
                    })
                    .collect(Collectors.toList()), // 合并所有结果为一个集合，并返回
                processingExecutor // 使用指定的线程池执行异步任务
        );
    }

    /**
     * 同步获取指定URL的HTML内容
     * @param url
     * @return
     */
    private String synFetchHtmlContent(String url) {
        // 构建HTML Get请求
        Request request = new Request.Builder()
                .url(url) // 设置要访问的目标URL
                .header("User-Agent",   "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36") // 设置浏览器标识，模拟真实浏览器访问
                .header("Accept", "text/html") // 指定接受HTML格式的响应
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) { // 执行请求并自动关闭响应资源
            // 检查响应状态和内容
            if (!response.isSuccessful() || response.body() == null) { // 响应失败或没有内容
                return ""; // 返回空字符串
            }

            // 读取响应体内容并返回
            return response.body().string();
        } catch (Exception e) { // 捕获网络IO异常
            return ""; // 异常时返回空字符串
        }
    }

    /**
     * 异步获取单个SearchResultDTO对象对应的页面内容
     * @param result
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return
     */
    private CompletableFuture<SearchResultDTO> asyncFetchContentForResult(
            SearchResultDTO result,
            long timeout,
            TimeUnit unit
    ) {

        // 异步线程处理
        return CompletableFuture.supplyAsync(() -> {
            // 获取HTML内容
            String html = synFetchHtmlContent(result.getUrl());

            return SearchResultDTO.builder()
                    .url(result.getUrl())
                    .score(result.getScore())
                    .content(html)
                    .build();
        }, httpExecutor) // 使用指定的线程池执行异步任务
                .completeOnTimeout(createFallbackResult(result), timeout, unit) // 超时处理
                .exceptionally(e -> { // 异常处理
                    // 记录错误日志
                    log.error("## 获取页面内容异常, URL: {}", result.getUrl(), e);
                    return createFallbackResult(result);
                });
    }

    /**
     * 创建回退结果（请求失败时使用）
     */
    private SearchResultDTO createFallbackResult(SearchResultDTO searchResult) {
        return SearchResultDTO.builder()
                .url(searchResult.getUrl())
                .score(searchResult.getScore())
                .content("") // 空字符串表示获取页面内容失败
                .build();
    }
}
