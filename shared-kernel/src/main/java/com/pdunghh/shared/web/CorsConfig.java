package com.pdunghh.shared.web;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình CORS dùng chung chuẩn Enterprise cho toàn bộ Microservices (auth-service, chess-service).
 * <p>
 * Đăng ký {@link CorsFilter} ở thứ tự ưu tiên cao nhất (HIGHEST_PRECEDENCE) để đảm bảo
 * các request preflight OPTIONS được xử lý và trả về header 200 OK ngay lập tức trước khi
 * chạm tới bất kỳ Filter nào khác (như RequestContextFilter, Spring Security filter chain).
 * </p>
 *
 * @author ChessHH Team
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173}")
    private String[] allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 1. Cấu hình Allowed Origins (Hỗ trợ Cookie / Credentials thì không dùng wildcard "*")
        config.setAllowedOrigins(Arrays.asList(allowedOrigins));

        // 2. Cho phép các HTTP Methods thông dụng
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));

        // 3. Cho phép tất cả Headers từ client gửi lên (Authorization, Content-Type, X-Trace-Id, ...)
        config.setAllowedHeaders(List.of("*"));

        // 4. Cho phép gửi Credentials (Cookie refreshToken, Session, Authorization headers)
        config.setAllowCredentials(true);

        // 5. Expose các headers quan trọng để Frontend JS đọc được
        config.setExposedHeaders(List.of("Authorization", "X-Trace-Id", "Set-Cookie"));

        // 6. Cache Preflight OPTIONS response trong 1 giờ (3600s) giúp giảm tải request OPTIONS liên tục
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean(CorsFilter corsFilter) {
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(corsFilter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "X-Trace-Id", "Set-Cookie")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
