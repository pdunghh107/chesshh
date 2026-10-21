package com.pdunghh.chess.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Cấu hình OpenAPI / Swagger UI cho Chess Service.
 * <p>
 * Định nghĩa tài liệu hóa API cho các nghiệp vụ chơi cờ, tìm trận
 * (matchmaking),
 * thống kê elo và bảng xếp hạng người chơi.
 * </p>
 *
 * @author ChessHH Team
 * @see <a href="https://springdoc.org">SpringDoc OpenAPI Documentation</a>
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "ChessHH - Game & Matchmaking Service API", version = "v1.0", description = "Hệ thống API quản lý ván cờ trực tuyến, tìm kiếm đối thủ (matchmaking), quản lý phòng chơi và tính điểm Elo.", contact = @Contact(name = "ChessHH Development Team")), security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", description = "Nhập Access Token (JWT) vào đây theo định dạng: Bearer <token>")
public class OpenApiConfig {
}
