// 2026/10/29 gongml [需求 待补充] 用户查询服务
package com.example.demo.user.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.api.PageResult;
import com.example.demo.user.dto.UserVO;
import com.example.demo.user.entity.SysUser;
import com.example.demo.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public PageResult<UserVO> page(long page, long size) {
        Page<SysUser> result = userMapper.selectPage(
                new Page<>(page, size),
                Wrappers.<SysUser>lambdaQuery().orderByDesc(SysUser::getId)
        );
        List<UserVO> list = result.getRecords().stream()
                .map(user -> new UserVO(
                        user.getId(),
                        user.getUsername(),
                        user.getNickname(),
                        user.getStatus(),
                        user.getCreatedAt()
                ))
                .toList();
        return new PageResult<>(list, result.getTotal(), result.getCurrent(), result.getSize());
    }
}
