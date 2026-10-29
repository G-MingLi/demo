// 2026/10/29 gongml [需求 待补充] 统一错误码
package com.example.demo.common.api;

public enum ErrorCode {
    SUCCESS(0, "ok"),
    BAD_REQUEST(10001, "参数错误"),
    UNAUTHORIZED(20001, "未登录或登录已过期"),
    FORBIDDEN(20002, "没有权限"),
    LOGIN_FAILED(20003, "用户名或密码错误"),
    USER_DISABLED(20004, "账号已被禁用"),
    TOKEN_INVALID(20005, "登录已过期，请重新登录"),
    INTERNAL_ERROR(50000, "系统繁忙，请稍后再试");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
