package com.research.common.core.exception;

import com.research.common.core.domain.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * 全局异常处理器（兼容 Gateway（WebFlux）和普通 Web 模块）
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 统一处理所有未捕获的 Exception
     * @param e 异常对象
     * @param request 请求对象（兼容 Servlet/WebFlux）
     * @return 标准化返回结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResult<String> handleException(Exception e, WebRequest request) {
        // 打印异常日志
        log.error("全局异常捕获：", e);

        // 兼容获取请求路径（避免强依赖 ServletWebRequest）
        String path = getRequestPath(request);

        // 返回标准化的错误结果
        return CommonResult.fail(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "系统异常：" + e.getMessage(),
                path
        );
    }

    /**
     * 兼容获取请求路径（适配 Servlet/WebFlux）
     */
    private String getRequestPath(WebRequest request) {
        try {
            // 反射方式获取请求路径，避免编译期依赖 Servlet API
            Object nativeRequest = request.getAttribute("org.springframework.web.context.request.ServletRequestAttributes.REFERENCE_REQUEST", WebRequest.SCOPE_REQUEST);
            if (nativeRequest != null) {
                return (String) nativeRequest.getClass().getMethod("getRequestURI").invoke(nativeRequest);
            }
        } catch (Exception ex) {
            log.warn("获取请求路径失败（非Servlet环境）", ex);
        }
        // 非Servlet环境返回上下文路径
        return request.getContextPath();
    }

    // 业务异常处理（可选）
    /*
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult<String> handleBusinessException(BusinessException e) {
        log.error("业务异常：", e);
        return CommonResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }
    */
}