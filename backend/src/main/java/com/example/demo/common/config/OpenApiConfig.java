// 2026/10/29 gongml [需求 待补充] OpenAPI 文档配置
package com.example.demo.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI demoOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Demo API")
                .description("Vue 3 + Spring Boot 基础架构接口")
                .version("v1"));
    }
}
