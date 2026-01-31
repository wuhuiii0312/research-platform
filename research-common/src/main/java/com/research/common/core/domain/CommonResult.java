package com.research.common.core.domain;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * 全局统一返回结果
 * 新增 unauthorized 方法，适配网关未授权场景
 */
@Data
public class CommonResult<T> {
    // 响应码
    private int code;
    // 响应消息
    private String msg;
    // 响应数据
    private T data;

    // 静态构造方法（成功）
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(HttpStatus.OK.value(), "操作成功", data);
    }

    // 静态构造方法（失败）
    public static <T> CommonResult<T> fail(int code, String msg) {
        return new CommonResult<>(code, msg, null);
    }

    // 重载：失败（带错误码+消息+附加数据）
    public static <T> CommonResult<T> fail(int code, String msg, T data) {
        return new CommonResult<>(code, msg, data);
    }

    // ========== 新增 unauthorized 方法 ==========
    /**
     * 未授权（401）返回结果
     * @param msg 错误消息
     * @return CommonResult
     */
    public static <T> CommonResult<T> unauthorized(String msg) {
        return new CommonResult<>(HttpStatus.UNAUTHORIZED.value(), msg, null);
    }

    // 构造方法
    public CommonResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}