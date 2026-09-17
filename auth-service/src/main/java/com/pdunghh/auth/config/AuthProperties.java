package com.pdunghh.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        String jwtSecret,
        String jwtIssuer,
        long accessTokenMinutes,
        long refreshTokenDays) {

}
