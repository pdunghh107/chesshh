package com.pdunghh.auth.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.pdunghh.auth.dto.response.LoginResponse;
import com.pdunghh.auth.dto.response.UserResponse;
import com.pdunghh.auth.entity.User;
import com.pdunghh.auth.exception.AuthException;
import com.pdunghh.auth.repository.UserRepository;
import com.pdunghh.auth.service.RedisTokenService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenServiceImpl implements RedisTokenService {

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh_token:";
    private static final String USER_TOKENS_KEY_PREFIX = "auth:user_tokens:";

    @Override
    public LoginResponse generateLoginResponse(User user) {
        String accessToken = createAccessToken(user);
        String refreshTokenStr = createAndSaveRefreshToken(user);

        return new LoginResponse(accessToken, refreshTokenStr, UserResponse.fromUser(user));
    }

    @Override
    public void revokeAllTokens(User user) {
        String userIdStr = user.getId().toString();
        String userTokensKey = USER_TOKENS_KEY_PREFIX + userIdStr;

        Set<String> tokenSet = redisTemplate.opsForSet().members(userTokensKey);
        if (tokenSet != null && !tokenSet.isEmpty()) {
            for (String token : tokenSet) {
                redisTemplate.delete(REFRESH_TOKEN_KEY_PREFIX + token);
            }
        }

        redisTemplate.delete(userTokensKey);
        log.info("Revoked all Redis refresh tokens for user: {}", userIdStr);
    }

    @Override
    public void revokeToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        String tokenKey = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        String userIdStr = redisTemplate.opsForValue().get(tokenKey);

        redisTemplate.delete(tokenKey);

        if (userIdStr != null && !userIdStr.isBlank()) {
            String userTokensKey = USER_TOKENS_KEY_PREFIX + userIdStr;
            redisTemplate.opsForSet().remove(userTokensKey, refreshToken);
        }

        log.info("Revoked Redis refresh token: {}", refreshToken);
    }

    @Override
    public LoginResponse refreshTokens(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw AuthException.invalidCredentials();
        }

        String tokenKey = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        String userIdStr = redisTemplate.opsForValue().get(tokenKey);

        if (userIdStr == null || userIdStr.isBlank()) {
            log.warn("Refresh token not found or expired in Redis: {}", refreshToken);
            throw AuthException.invalidCredentials();
        }

        UUID userId = UUID.fromString(userIdStr);
        User user = userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(AuthException::userNotFound);

        if (!user.isActive()) {
            throw AuthException.accountDeactivated();
        }

        // Token Rotation: Thu hoi refresh token cu truoc khi cap token moi
        revokeToken(refreshToken);

        return generateLoginResponse(user);
    }

    private String createAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole())
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private String createAndSaveRefreshToken(User user) {
        String refreshTokenStr = UUID.randomUUID().toString();
        String userIdStr = user.getId().toString();

        String tokenKey = REFRESH_TOKEN_KEY_PREFIX + refreshTokenStr;
        String userTokensKey = USER_TOKENS_KEY_PREFIX + userIdStr;

        // 1. Mapping auth:refresh_token:{uuid} -> userId (TTL)
        redisTemplate.opsForValue().set(tokenKey, Objects.requireNonNull(userIdStr), refreshExpirationMs,
                TimeUnit.MILLISECONDS);

        // 2. Mapping auth:user_tokens:{userId} -> Set of tokens (TTL)
        redisTemplate.opsForSet().add(userTokensKey, refreshTokenStr);
        redisTemplate.expire(userTokensKey, refreshExpirationMs, TimeUnit.MILLISECONDS);

        log.info("Saved Redis refresh token for user: {}", userIdStr);
        return refreshTokenStr;
    }
}
