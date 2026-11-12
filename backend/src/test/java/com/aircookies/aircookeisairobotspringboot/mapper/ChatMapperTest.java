package com.aircookies.aircookeisairobotspringboot.mapper;

import com.aircookies.aircookeisairobotspringboot.domain.dos.ChatDO;
import com.aircookies.aircookeisairobotspringboot.domain.mapper.ChatMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
public class ChatMapperTest {

    @Resource
    private ChatMapper chatMapper;

    /**
     * 测试添加数据
     */
    @Test
    void testInsert() {
        chatMapper.insert(ChatDO.builder()
                .uuid(UUID.randomUUID().toString())
                .summary("测试数据")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build()
        );

    }
}
