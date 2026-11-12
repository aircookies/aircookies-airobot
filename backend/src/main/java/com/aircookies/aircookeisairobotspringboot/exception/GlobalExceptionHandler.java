package com.aircookies.aircookeisairobotspringboot.exception;

import com.aircookies.aircookeisairobotspringboot.enums.ResponseCodeEnum;
import com.aircookies.aircookeisairobotspringboot.utils.Response;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.IOException;
import java.util.Optional;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获自定义异常
     * @return
     */
    @ExceptionHandler({BizException.class})
    @ResponseBody
    public Response<Object> handleBizException(HttpServletRequest request, BizException e) {
        log.warn("{} request fail, errorCode: {}, errorMessage: {}", request.getRequestURI(), e.getErrorCode(), e.getErrorMessage());
        return Response.fail(e);
    }

    /**
     * 捕获参数校验异常
     * @return
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseBody
    public Response<Object> handleMethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
        // 参数错误异常码
        String errorCode = ResponseCodeEnum.PARAM_NOT_VALID.getErrorCode();

        // 获取BindingResult
        BindingResult bindingResult = e.getBindingResult();

        StringBuilder sb = new StringBuilder();

        // 获取校验不通过的字段，并组合错误信息，格式：email邮箱格式不正确,当前值: '1234567qq.com';
        Optional.ofNullable(bindingResult.getFieldErrors()).ifPresent(errors -> {
            errors.forEach(error ->
                    sb.append(error.getField())
                            .append(" ")
                            .append(error.getDefaultMessage())
                            .append(", 当前值: '")
                            .append(error.getRejectedValue())
                            .append("'; ")

            );
        });

        // 错误信息
        String errorMessage = sb.toString();

        log.warn("{} request error, errorCode: {}, errorMessage: {}", request.getRequestURI(), errorCode, errorMessage);

        return Response.fail(errorCode, errorMessage);
    }

    /**
     * 客户端主动断开连接(如 SSE 流式输出时前端关闭页面/取消请求)
     * 属于正常现象,仅记录 warn 日志,不打印堆栈、不再写响应
     */
    @ExceptionHandler({ClientAbortException.class, AsyncRequestNotUsableException.class})
    public void handleClientAbortException(HttpServletRequest request, Exception e) {
        log.warn("{} 客户端连接已断开, {}", request.getRequestURI(), e.getMessage());
    }

    /**
     * IO 异常:先判断是否为客户端断连,是则降级处理;否则按系统错误兜底
     */
    @ExceptionHandler(IOException.class)
    @ResponseBody
    public Response<Object> handleIOException(HttpServletRequest request, IOException e) {
        if (isClientAbortException(e)) {
            log.warn("{} 客户端连接已断开, 停止输出: {}", request.getRequestURI(), e.getMessage());
            return null;
        }
        log.error("{} request error, ", request.getRequestURI(), e);
        return Response.fail(ResponseCodeEnum.SYSTEM_ERROR);
    }

    /**
     * 其他类型异常
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler({Exception.class})
    @ResponseBody
    public Response<Object> handleOtherException(HttpServletRequest request, Exception e) {
        log.error("{} request error, ", request.getRequestURI(), e);
        return Response.fail(ResponseCodeEnum.SYSTEM_ERROR);
    }


    private boolean isClientAbortException(Throwable e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof ClientAbortException || t instanceof AsyncRequestNotUsableException) {
                return true;
            }
            String message = t.getMessage();
            if (message != null && (message.contains("中止")
                    || message.contains("断开的管道")
                    || message.contains("远程主机强迫关闭")
                    || message.toLowerCase().contains("aborted")
                    || message.toLowerCase().contains("broken pipe")
                    || message.toLowerCase().contains("connection reset"))) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }
}
