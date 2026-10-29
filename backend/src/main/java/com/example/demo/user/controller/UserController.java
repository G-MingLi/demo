// 2026/10/29 gongml [需求 待补充] 用户查询接口
package com.example.demo.user.controller;

import com.example.demo.common.api.ApiResponse;
import com.example.demo.common.api.PageResult;
import com.example.demo.user.dto.UserVO;
import com.example.demo.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user:list')")
    public ApiResponse<PageResult<UserVO>> list(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size) {
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);
        return ApiResponse.ok(userService.page(safePage, safeSize));
    }
}
