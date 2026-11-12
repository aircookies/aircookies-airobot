package com.aircookies.aircookeisairobotspringboot.config;

import com.aircookies.aircookeisairobotspringboot.utils.JsonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapper jsonMapper() {

        JsonMapper jsonMapper = JsonMapper.builder()
                // 忽略未知属性（Jackson 3 中该特性默认已关闭，此处显式声明以便阅读）
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // 允许序列化空对象（Jackson 3 中该特性默认也已关闭）
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                // 设置凡是为 null 的字段，返参中均不返回，请根据项目组约定是否开启
                // .changeDefaultPropertyInclusion(JsonInclude.Value.construct(
                //         JsonInclude.Include.NON_NULL, JsonInclude.Include.ALWAYS))
                // 设置时区
                .defaultTimeZone(TimeZone.getTimeZone("Asia/Shanghai"))
                // 无需注册 JavaTimeModule：Jackson 3 已内置 java.time（LocalDateTime 等）支持
                .build();

        // 初始化JsonUtil中的JSON_MAPPER
        JsonUtil.init(jsonMapper);

        return jsonMapper;
    }
}
