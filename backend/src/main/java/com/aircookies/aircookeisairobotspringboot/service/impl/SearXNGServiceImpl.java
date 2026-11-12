package com.aircookies.aircookeisairobotspringboot.service.impl;

import com.aircookies.aircookeisairobotspringboot.model.dto.SearchResultDTO;
import com.aircookies.aircookeisairobotspringboot.service.SearXNGService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class SearXNGServiceImpl implements SearXNGService {

    @Resource
    private OkHttpClient okHttpClient;
    @Resource
    private JsonMapper jsonMapper;
    @Value("${searxng.url}")
    private String searxngUrl;
    @Value("${searxng.count}")
    private int count;

    /**
     * 调用SearXNG接口进行搜索
     *
     * @param query 搜索关键词
     * @return 搜索结果
     */
    @Override
    public List<SearchResultDTO> search(String query) {
        // 构建SearXNG接口请求
        HttpUrl httpUrl = HttpUrl.parse(searxngUrl).newBuilder()
                .addQueryParameter("q", query) // 设置搜索关键词
                .addQueryParameter("format", "json") // 返回指定Json格式
                .addQueryParameter("engines", "bing,quark,sogou,360search") // 指定聚合的目标搜索引擎
                .build();

        // 构建Http Get请求
        Request request = new Request.Builder()
                .url(httpUrl)
                .get()
                .build();

        // 发送Http请求
        try (Response response = okHttpClient.newCall(request).execute()) {
            // 判断请求是否成功
            if (response.isSuccessful()) {
                // 拿到返回结果
                String result = null;
                if (response.body() != null) {
                    result = response.body().string();
                }
                log.info("SearXNG搜索结果: {}", result);

                // 解析Json响应
                JsonNode root = jsonMapper.readTree(result);
                JsonNode results = root.get("results"); //获取结果数组节点

                // 定义Record类型: 用于临时存储评分和节点引用
                record NodeWithUrlAndScore(double score, JsonNode node) {
                }

                // 处理搜索结果流:
                // 1. 提取评分
                // 2. 按评分降序排序
                // 3. 限制返回结果数量(比如只提取评分最高的50条搜索结果)
                List<NodeWithUrlAndScore> nodesWithScore = StreamSupport.stream(results.spliterator(), false)
                        .map(node -> {
                            // 只提取分数, 避免构建完整对象
                            double score = node.path("score").asDouble(0.0); //提取评分
                            return new NodeWithUrlAndScore(score, node);
                        })
                        .sorted(Comparator.comparingDouble(NodeWithUrlAndScore::score).reversed()) // 按评分排序
                        .limit(count) // 限制返回结果数量
                        .toList();

                // 转换为SearchResultDTO对象集合
                return nodesWithScore.stream()
                        .map(n -> {
                            JsonNode node = n.node();
                            String originalUrl = node.path("url").asString(""); // 提取URL
                            return SearchResultDTO.builder()
                                    .url(originalUrl)
                                    .score(n.score()) // 保留评分
                                    .build();
                        })
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            log.error("", e);
        }

        //返回空集合
        return Collections.emptyList();
    }
}
