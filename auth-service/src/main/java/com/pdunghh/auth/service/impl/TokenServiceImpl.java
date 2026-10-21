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
    public LoginResponse buildLoginResponse(User user) {
        String accessToken = createAccessToken(user);
        String refreshTokenStr = createRefreshToken(user);
        return new LoginResponse(accessToken, refreshTokenStr, UserResponse.of(user));
    }

    @Override
    public void revokeAllRefreshTokensByUser(User user) {
        refreshTokenRepository.revokeAllRefreshTokensByUser(user, Instant.now());
    }

    @Override
    public void revokeRefreshTokenIfPresent(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(this::saveRefreshTokenRevokedState);
    }

    @Override
    public LoginResponse refreshTokens(String refreshToken) {
        RefreshToken tokenEntity = findRefreshToken(refreshToken);
        User user = tokenEntity.getUser();
        checkRefreshTokenDataValid(tokenEntity, user);
        saveRefreshTokenRevokedState(tokenEntity);
        return buildLoginResponse(user);
    }

    // [UTILS]

    // [OUT EXCEPTION]
    private RefreshToken findRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken).orElseThrow(AuthException::invalidCredentials);
    }

    private void checkRefreshTokenValid(RefreshToken token) {
        if (token.getRevokedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw AuthException.invalidCredentials();
        }
    }

    private void checkUserActive(User user) {
        user.checkUserActive();
    }

    // [PRE HANDLE]

    private void checkRefreshTokenDataValid(RefreshToken refreshToken, User user) {
        checkRefreshTokenValid(refreshToken);
        checkUserActive(user);
    }

    // [SAVE TO DB]
    private RefreshToken saveRefreshTokenData(User user, String refreshTokenStr) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .build();
        return refreshTokenRepository.save(Objects.requireNonNull(refreshToken));
    }

    private void saveRefreshTokenRevokedState(RefreshToken refreshToken) {
        refreshToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);
    }

    // [MAIN FUNCTIONS]
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
        saveRefreshTokenData(user, refreshTokenStr);
        return refreshTokenStr;
    }

    // [POST HANDLE, SIDE EFFECTS]
}
