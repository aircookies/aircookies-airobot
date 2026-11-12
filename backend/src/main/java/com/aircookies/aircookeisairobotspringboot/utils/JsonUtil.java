package com.aircookies.aircookeisairobotspringboot.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonUtil {

    private static JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    /**
     * 初始化JsonMapper
     *
     * @param JSON_MAPPER
     */

    public static void init(JsonMapper JSON_MAPPER) {
        JsonUtil.JSON_MAPPER = JSON_MAPPER;
    }

    /**
     * 将对象转换为Json字符串
     *
     * @param obj
     * @return
     */
    @SneakyThrows
    public static String toJson(Object obj) {
        return JSON_MAPPER.writeValueAsString(obj);
    }

    /**
     * 将Json字符串转换为对象
     *
     * @param jsonStr
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T parseObject(String jsonStr, Class<T> clazz) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }

        return JSON_MAPPER.readValue(jsonStr, clazz);
    }

    /**
     * 将Json字符串转换为Map
     *
     * @param jsonStr
     * @param keyClazz
     * @param valueClazz
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> parseMap(String jsonStr, Class<K> keyClazz, Class<V> valueClazz) {
        return JSON_MAPPER.readValue(jsonStr, JSON_MAPPER.getTypeFactory().constructMapType(Map.class, keyClazz, valueClazz));
    }

    /**
     * 将Json字符串转换为List
     *
     * @param jsonStr
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> parseList(String jsonStr, Class<T> clazz) {
        return JSON_MAPPER.readValue(jsonStr, JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    /**
     * 将Json字符串转换为Set
     *
     * @param jsonStr
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> Set<T> parseSet(String jsonStr, Class<T> clazz) {
        return JSON_MAPPER.readValue(jsonStr, JSON_MAPPER.getTypeFactory().constructCollectionType(Set.class, clazz));
    }
}
