// 2026/10/29 gongml [需求 待补充] 刷新令牌存储接口
package com.example.demo.auth.store;

import java.time.Duration;

public interface RefreshTokenStore {

    void save(String token, Long userId, Duration ttl);

    Long get(String token);

    void remove(String token);
}
