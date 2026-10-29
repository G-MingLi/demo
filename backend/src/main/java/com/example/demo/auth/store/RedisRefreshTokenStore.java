// 2026/10/29 gongml [需求 待补充] Redis 刷新令牌存储
package com.example.demo.auth.store;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConditionalOnProperty(name = "app.auth.refresh-store", havingValue = "redis", matchIfMissing = true)
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String PREFIX = "auth:refresh:";

    private final StringRedisTemplate redisTemplate;

    public RedisRefreshTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String token, Long userId, Duration ttl) {
        redisTemplate.opsForValue().set(PREFIX + token, String.valueOf(userId), ttl);
    }

    @Override
    public Long get(String token) {
        String value = redisTemplate.opsForValue().get(PREFIX + token);
        return value == null ? null : Long.valueOf(value);
    }

    @Override
    public void remove(String token) {
        redisTemplate.delete(PREFIX + token);
    }
}
