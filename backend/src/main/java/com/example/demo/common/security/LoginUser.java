// 2026/10/29 gongml [需求 待补充] 当前登录用户
package com.example.demo.common.security;

import java.util.List;

public record LoginUser(
        Long id,
        String username,
        String nickname,
        List<String> permissions
) {
}
