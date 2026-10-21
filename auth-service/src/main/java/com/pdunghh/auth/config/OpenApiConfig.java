package com.pdunghh.auth.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Cấu hình OpenAPI / Swagger UI cho Auth Service.
 * <p>
 * Định nghĩa tài liệu hóa API chuẩn RESTful, tích hợp hỗ trợ Bearer Token (JWT)
 * để kiểm thử trực tiếp các endpoint cần xác thực trên Swagger UI.
 * </p>
 *
 * @author ChessHH Team
 * @see <a href="https://springdoc.org">SpringDoc OpenAPI Documentation</a>
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ChessHH - Authentication & User Service API",
                version = "v1.0",
                description = "Hệ thống API quản lý tài khoản người dùng, xác thực danh tính (JWT), quản lý phiên và phân quyền.",
                contact = @Contact(name = "ChessHH Development Team")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Nhập Access Token (JWT) vào đây theo định dạng: Bearer <token>"
)
public class OpenApiConfig {
}
