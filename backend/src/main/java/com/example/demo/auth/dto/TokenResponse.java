// 2026/10/29 gongml [需求 待补充] 登录令牌响应
package com.example.demo.auth.dto;

import com.example.demo.common.security.LoginUser;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        LoginUser user
) {
}
