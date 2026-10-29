// 2026/10/29 gongml [需求 待补充] 登录/刷新/登出服务
package com.example.demo.auth;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.auth.dto.LoginRequest;
import com.example.demo.auth.dto.TokenResponse;
import com.example.demo.auth.store.RefreshTokenStore;
import com.example.demo.common.api.ErrorCode;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.security.JwtService;
import com.example.demo.common.security.LoginUser;
import com.example.demo.user.entity.SysUser;
import com.example.demo.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;
    private final Duration refreshTokenTtl;

    public AuthService(UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenStore refreshTokenStore,
                       @Value("${app.auth.refresh-token-ttl:P7D}") Duration refreshTokenTtl) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenStore = refreshTokenStore;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public TokenResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getUsername, request.username())
                        .last("LIMIT 1")
        );
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        return issueTokens(user);
    }

    public TokenResponse refresh(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        Long userId = refreshTokenStore.get(refreshToken);
        if (userId == null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        // 轮换刷新令牌：旧令牌立即失效
        refreshTokenStore.remove(refreshToken);

        SysUser user = userMapper.selectById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        return issueTokens(user);
    }

    public void logout(String refreshToken) {
        if (StringUtils.hasText(refreshToken)) {
            refreshTokenStore.remove(refreshToken);
        }
    }

    public LoginUser me(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        List<String> permissions = userMapper.selectPermissionCodes(userId);
        return new LoginUser(user.getId(), user.getUsername(), user.getNickname(), permissions);
    }

    private TokenResponse issueTokens(SysUser user) {
        List<String> permissions = userMapper.selectPermissionCodes(user.getId());
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getNickname(), permissions);
        String accessToken = jwtService.createAccessToken(loginUser);
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        refreshTokenStore.save(refreshToken, user.getId(), refreshTokenTtl);
        return new TokenResponse(accessToken, refreshToken, loginUser);
    }
}
