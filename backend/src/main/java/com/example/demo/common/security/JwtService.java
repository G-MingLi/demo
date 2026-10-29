// 2026/10/29 gongml [需求 待补充] JWT 签发与解析
package com.example.demo.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final SecretKey key;
    private final Duration accessTokenTtl;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-token-ttl:PT30M}") Duration accessTokenTtl) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("app.jwt.secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.accessTokenTtl = accessTokenTtl;
    }

    public String createAccessToken(LoginUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.id()))
                .claim("username", user.username())
                .claim("nickname", user.nickname())
                .claim("permissions", user.permissions() == null ? List.of() : user.permissions())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .signWith(key)
                .compact();
    }

    public LoginUser parseUser(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("missing subject");
        }

        @SuppressWarnings("unchecked")
        List<Object> rawPermissions = claims.get("permissions", List.class);
        List<String> permissions = rawPermissions == null
                ? List.of()
                : rawPermissions.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();

        return new LoginUser(
                Long.valueOf(subject),
                claims.get("username", String.class),
                claims.get("nickname", String.class),
                permissions
        );
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtl.toSeconds();
    }
}
