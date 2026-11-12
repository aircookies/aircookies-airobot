package com.aircookies.aircookeisairobotspringboot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum {


    // 通用异常状态码
    SYSTEM_ERROR("10000", "发生错误"),
    PARAM_NOT_VALID("10001", "参数错误"),

    // 业务异常状态码
    CHAT_NOT_EXISTED("20000", "此对话不存在"),
    UPLOAD_FILE_CANT_EMPTY("20001", "上传文件不能为空"),
    ONLY_SUPPORT_MARKDOWN("20002", "只支持上传Markdown文件"),
    UPLOAD_FILE_FAILED("20003", "上传文件失败"),
    MARKDOWN_FILE_NOT_FOUND("20004", "Markdown 问答文件不存在"),
    MARKDOWN_FILE_CANT_DELETE("20005", "正在处理中的 Markdown 问答文件，不允许删除"),
    ;
    private final String errorCode;
    private final String errorMessage;

    }
