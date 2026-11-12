package com.aircookies.aircookeisairobotspringboot.utils;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {

    /**
     * 截取用户问题的千米那部分文字作为摘要
     *
     * @param message 用户问题
     * @param maxLength 最大截取长度
     * @return 摘要文本，如果问题长度不足则返回原问题
     */
    public static String truncate(String message, int maxLength) {
        // 判空
        if (StringUtils.isBlank(message)) {
            return "";
        }

        String trimmed = message.trim();

        // 如果文本长度小于等于最大长度，直接返回
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }

        // 截取指定长度
        return trimmed.substring(0, maxLength);
    }
}
