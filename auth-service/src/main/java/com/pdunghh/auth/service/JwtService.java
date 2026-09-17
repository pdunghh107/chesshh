package com.pdunghh.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.pdunghh.auth.config.AuthProperties;
import com.pdunghh.auth.entity.RefreshTokenEntity;
import com.pdunghh.auth.entity.UserEntity;
import com.pdunghh.auth.repository.RefreshTokenRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j

public final class JwtService {

    private final AuthProperties authProperties;
    private final SecretKey secretKey;
    private final RefreshTokenRepository tokenRepository;

    public String createAccessToken(UserEntity user, String role) {
        Instant now = Instant.now();
        Date expration = Date.from(now.plus(authProperties.accessTokenMinutes(), ChronoUnit.MINUTES));

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .issuer(authProperties.jwtIssuer())
                .issuedAt(Date.from(now))
                .expiration(expration)
                .signWith(secretKey)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String createRefresh(UserEntity user) {
        String token = UUID.randomUUID().toString();
        RefreshTokenEntity entity = toCreate(user, token);
        tokenRepository.save(Objects.requireNonNull(entity));
        return token;
    }

    private RefreshTokenEntity toCreate(UserEntity user, String token) {
        OffsetDateTime expiresAt = OffsetDateTime.now().plus(authProperties.refreshTokenDays(), ChronoUnit.DAYS);
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUser(user);
        entity.setTokenHash(sha256hex(token));
        entity.setExpiresAt(expiresAt);
        return entity;
    }

    private String sha256hex(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawValue.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Môi trường JVM hiện tại không hỗ trợ thuật toán SHA-256", ex);
        }
    }
}
