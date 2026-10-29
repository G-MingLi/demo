// 2026/10/29 gongml [需求 待补充] 前端登录响应
package com.example.demo.auth.dto;

import com.example.demo.common.security.LoginUser;

public record LoginResponse(
        String accessToken,
        LoginUser user
) {
}
