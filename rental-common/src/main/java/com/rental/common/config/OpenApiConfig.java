package com.rental.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 3.0 配置
 * 访问地址：http://localhost:8080/admin/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    /**
     * OpenAPI 3.0 配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 基本信息
                .info(new Info()
                        .title("🏠 房租管理系统 API")
                        .description("""
                                ## 轻量级房租管理系统接口文档
                                
                                ### 🎯 核心功能
                                - **房东认证**：注册、登录、验证码
                                - **房间管理**：CRUD、费用管理、租客绑定
                                - **抄表管理**：批量抄表、用量计算、统计查询
                                
                                ### 🔐 认证说明
                                1. 除认证接口外，所有接口都需要 JWT Token
                                2. Token 通过 `Authorization: Bearer <token>` 传递
                                3. Token 有效期 24 小时
                                
                                ### 📱 使用场景
                                - **房东端**：管理房产、房间、抄表、账单
                                - **租客端**：查看房间信息、账单、支付记录
                                
                                ### 🛠️ 技术栈
                                - Java 17 + Spring Boot 3.2.5
                                - MyBatis Plus + MySQL 8.0
                                - Redis + JWT
                                
                                ### 📖 使用说明
                                1. 先调用认证接口获取Token
                                2. 点击右上角🔒按钮输入Token
                                3. 格式：`Bearer your-jwt-token`
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@rental.com")
                                .url("https://github.com/rental-system"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                
                // 服务器配置
                .addServersItem(new Server()
                        .url("http://localhost:8080/admin")
                        .description("本地开发环境"))
                .addServersItem(new Server()
                        .url("https://api.rental.com")
                        .description("生产环境"))
                
                // JWT认证配置
                .components(new Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("请输入 JWT Token，格式：Bearer <token>")))
                
                // 全局安全要求
                .addSecurityItem(new SecurityRequirement().addList("Bearer"));
    }
}