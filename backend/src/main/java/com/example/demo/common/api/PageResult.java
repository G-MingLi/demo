// 2026/10/29 gongml [需求 待补充] 分页响应结构
package com.example.demo.common.api;

import java.util.List;

public record PageResult<T>(
        List<T> list,
        long total,
        long page,
        long size
) {
}
