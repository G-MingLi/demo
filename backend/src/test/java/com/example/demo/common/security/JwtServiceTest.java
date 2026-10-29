// 2026/10/29 gongml [需求 待补充] JWT 单元测试
package com.example.demo.common.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "test-secret-for-jwt-unit-test-32bytes-minimum",
            Duration.ofMinutes(30)
    );

    @Test
    void shouldCreateAndParseAccessToken() {
        LoginUser user = new LoginUser(1L, "admin", "管理员", List.of("user:list", "dashboard:view"));

        String token = jwtService.createAccessToken(user);
        LoginUser parsed = jwtService.parseUser(token);

        assertEquals(1L, parsed.id());
        assertEquals("admin", parsed.username());
        assertEquals("管理员", parsed.nickname());
        assertEquals(List.of("user:list", "dashboard:view"), parsed.permissions());
        assertTrue(jwtService.getAccessTokenTtlSeconds() > 0);
    }
}
