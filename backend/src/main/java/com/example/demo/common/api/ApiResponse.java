// 2026/10/29 gongml [需求 待补充] 统一 API 响应结构
package com.example.demo.common.api;

import org.slf4j.MDC;

import java.time.OffsetDateTime;

public record ApiResponse<T>(
        int code,
        String message,
        T data,
        String traceId,
        OffsetDateTime timestamp
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(), "ok", data, currentTraceId(), OffsetDateTime.now());
    }

    public static ApiResponse<Void> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null, currentTraceId(), OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }

    private static String currentTraceId() {
        String traceId = MDC.get("traceId");
        return traceId == null ? "" : traceId;
    }
}
