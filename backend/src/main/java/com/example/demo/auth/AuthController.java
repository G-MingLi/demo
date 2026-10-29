// 2026/10/29 gongml [需求 待补充] 认证接口
package com.example.demo.auth;

import com.example.demo.auth.dto.LoginRequest;
import com.example.demo.auth.dto.LoginResponse;
import com.example.demo.auth.dto.TokenResponse;
import com.example.demo.common.api.ApiResponse;
import com.example.demo.common.api.ErrorCode;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.security.LoginUser;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private static final String REFRESH_COOKIE = "refresh_token";
    private static final String COOKIE_PATH = "/api/v1/auth";

    private final AuthService authService;
    private final boolean cookieSecure;
    private final Duration refreshTokenTtl;

    public AuthController(AuthService authService,
                          @Value("${app.auth.cookie-secure:false}") boolean cookieSecure,
                          @Value("${app.auth.refresh-token-ttl:P7D}") Duration refreshTokenTtl) {
        this.authService = authService;
        this.cookieSecure = cookieSecure;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletResponse response) {
        TokenResponse token = authService.login(request);
        writeRefreshCookie(response, token.refreshToken());
        return ApiResponse.ok(new LoginResponse(token.accessToken(), token.user()));
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        TokenResponse token = authService.refresh(refreshToken);
        writeRefreshCookie(response, token.refreshToken());
        return ApiResponse.ok(new LoginResponse(token.accessToken(), token.user()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(refreshToken);
        clearRefreshCookie(response);
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<LoginUser> me(@AuthenticationPrincipal LoginUser loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ApiResponse.ok(authService.me(loginUser.id()));
    }

    private void writeRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path(COOKIE_PATH)
                .maxAge(refreshTokenTtl)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path(COOKIE_PATH)
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
