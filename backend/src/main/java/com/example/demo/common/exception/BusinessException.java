// 2026/10/29 gongml [需求 待补充] 业务异常
package com.example.demo.common.exception;

import com.example.demo.common.api.ErrorCode;

public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
