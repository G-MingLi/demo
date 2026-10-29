// 2026/10/29 gongml [需求 待补充] 用户列表视图
package com.example.demo.user.dto;

import java.time.LocalDateTime;

public record UserVO(
        Long id,
        String username,
        String nickname,
        Integer status,
        LocalDateTime createdAt
) {
}
