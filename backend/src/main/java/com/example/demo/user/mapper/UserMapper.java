// 2026/10/29 gongml [需求 待补充] 用户数据访问
package com.example.demo.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<SysUser> {

    @Select("""
            SELECT DISTINCT p.code
            FROM sys_permission p
            JOIN sys_role_permission rp ON rp.permission_id = p.id
            JOIN sys_user_role ur ON ur.role_id = rp.role_id
            WHERE ur.user_id = #{userId}
            ORDER BY p.code
            """)
    List<String> selectPermissionCodes(@Param("userId") Long userId);
}
