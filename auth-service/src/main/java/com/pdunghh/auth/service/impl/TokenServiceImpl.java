package com.pdunghh.auth.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pdunghh.auth.dto.response.LoginResponse;
import com.pdunghh.auth.dto.response.UserResponse;
import com.pdunghh.auth.entity.RefreshToken;
import com.pdunghh.auth.entity.User;
import com.pdunghh.auth.exception.AuthException;
import com.pdunghh.auth.repository.RefreshTokenRepository;
import com.pdunghh.auth.service.TokenService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Override
    public LoginResponse generateLoginResponse(User user) {

        String accessToken = createAccessToken(user);

        String refreshTokenStr = createRefreshToken(user);

        return new LoginResponse(accessToken, refreshTokenStr, UserResponse.fromUser(user));
    }

    @Override
    public void revokeAllTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user, Instant.now());
    }

    @Override
    public void revokeToken(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
        });
    }

    @Override
    public LoginResponse refreshTokens(String refreshToken) {
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(AuthException::invalidCredentials);

        if (tokenEntity.getRevokedAt() != null || tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            throw AuthException.invalidCredentials();
        }

        User user = tokenEntity.getUser();
        if (!user.isActive()) {
            throw AuthException.invalidCredentials();
        }

        // Revoke old refresh token for rotation
        tokenEntity.setRevokedAt(Instant.now());
        refreshTokenRepository.save(tokenEntity);

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

    private String createRefreshToken(User user) {
        String refreshTokenStr = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .build();

        refreshTokenRepository.save(Objects.requireNonNull(refreshToken));

        return refreshTokenStr;
    }

}
