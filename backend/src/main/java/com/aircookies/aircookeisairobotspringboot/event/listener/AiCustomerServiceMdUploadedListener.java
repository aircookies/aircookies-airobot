package com.aircookies.aircookeisairobotspringboot.event.listener;

import com.aircookies.aircookeisairobotspringboot.domain.dos.AiCustomerServiceMdStorageDO;
import com.aircookies.aircookeisairobotspringboot.domain.mapper.AiCustomerServiceMdStorageMapper;
import com.aircookies.aircookeisairobotspringboot.enums.AiCustomerServiceMdStatusEnum;
import com.aircookies.aircookeisairobotspringboot.event.AiCustomerServiceMdUploadedEvent;
import com.aircookies.aircookeisairobotspringboot.reader.MarkdownReader;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@Async("eventTaskExecutor") // 使用自定义线程池
public class AiCustomerServiceMdUploadedListener {

    @Resource
    private MarkdownReader markdownReader;
    @Resource
    private VectorStore vectorStore;
    @Resource
    private AiCustomerServiceMdStorageMapper aiCustomerServiceMdStorageMapper;
    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * Markdown文件向量化
     * @param event
     */
    @EventListener
    @Async("eventTaskExecutor") // 使用自定义线程池
    public void vectorizing(AiCustomerServiceMdUploadedEvent event) {
        log.info("## AiCustomerServiceMdUploadedEvent:{}", event);

        // 文件存储表主键ID
        Long id = event.getId();
        // Markdown文件存储路径
        String filePath = event.getFilePath();
        // 元数据
        Map<String, Object> metadatas = event.getMetadatas();

        // 更新存储文件的处理状态为"向量化中"
        aiCustomerServiceMdStorageMapper.updateById(AiCustomerServiceMdStorageDO.builder()
                .id(id)
                .status(AiCustomerServiceMdStatusEnum.VECTORIZING.getCode())
                .updateTime(LocalDateTime.now())
                .build());

        // 编程式事务
        boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                // 读取文件
                org.springframework.core.io.Resource resource = new FileSystemResource(filePath);

                // 解析为Document集合
                List<Document> documents = markdownReader.loadMarkdown(resource, metadatas);

                log.info("## documents: {}", documents);

                // 向量化，并存储入库
                for (Document document : documents) {
                    // 防止重复添加相同文档到数据库中
                    // 从向量数据中查询当前文档
                    List<Document> results = vectorStore.similaritySearch(SearchRequest.builder()
                            .query(document.getText())
                            .topK(1) // 查询一条最高得分的
                            .build());

                    // 如果结果不为空，并且得分大于 0.99，则表示文档较高几率重复，直接跳过
                    if (!results.isEmpty() && results.get(0).getScore() > 0.99) {
                        continue;
                    }

                    // 通过向量模型，将文档向量化存储到 PGVector 中
                    vectorStore.add(List.of(document));
                }

                // 更新存储文件的处理状态为 “已完成”
                aiCustomerServiceMdStorageMapper.updateById(AiCustomerServiceMdStorageDO.builder()
                        .id(id)
                        .status(AiCustomerServiceMdStatusEnum.COMPLETED.getCode())
                        .updateTime(LocalDateTime.now())
                        .build());

                return true;
            } catch (Exception e) {
                log.error("## Markdown 文件向量化失败: {}", event, e);
                status.setRollbackOnly(); // 标记事务为回滚
                return false;
            }
        }));

        // 若事务执行失败，更新存储文件的处理状态为 “失败”
        if (!isSuccess) {
            aiCustomerServiceMdStorageMapper.updateById(AiCustomerServiceMdStorageDO.builder()
                    .id(id)
                    .status(AiCustomerServiceMdStatusEnum.FAILED.getCode())
                    .updateTime(LocalDateTime.now())
                    .build());
        }
    }
}
