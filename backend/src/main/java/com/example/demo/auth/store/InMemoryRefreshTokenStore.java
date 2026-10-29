// 2026/10/29 gongml [需求 待补充] 本地内存刷新令牌存储
package com.example.demo.auth.store;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "app.auth.refresh-store", havingValue = "memory")
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

    private record Entry(Long userId, Instant expiresAt) {
    }

    private final Map<String, Entry> tokens = new ConcurrentHashMap<>();

    @Override
    public void save(String token, Long userId, Duration ttl) {
        tokens.put(token, new Entry(userId, Instant.now().plus(ttl)));
    }

    @Override
    public Long get(String token) {
        Entry entry = tokens.get(token);
        if (entry == null) {
            return null;
        }
        if (entry.expiresAt().isBefore(Instant.now())) {
            tokens.remove(token);
            return null;
        }
        return entry.userId();
    }

    @Override
    public void remove(String token) {
        tokens.remove(token);
    }
}
