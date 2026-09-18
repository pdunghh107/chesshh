package com.pdunghh.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.pdunghh.auth.config.AuthProperties;
import com.pdunghh.auth.entity.RefreshTokenEntity;
import com.pdunghh.auth.entity.UserEntity;
import com.pdunghh.auth.exception.AuthException;
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
    private final StringRedisTemplate stringRedisTemplate;
    private final RefreshTokenRepository tokenRepository;

    private static final String BLACKLIST_JTI_PREFIX = "token:blacklist:jti:";
    private static final String REVOKE_ALL_USER_PREFIX = "token:revoke_all:user:";

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

    public RefreshTokenEntity findRefreshToken(String token) {
        return tokenRepository.findByTokenHash(sha256hex(token))
                .orElseThrow(AuthException::invalidToken);
    }

    public void revokeTokenByJti(String jti, Date expiresAt) {
        long remainingMilis = expiresAt.getTime() - System.currentTimeMillis();
        String key = BLACKLIST_JTI_PREFIX + jti;
        if (remainingMilis > 0) {
            stringRedisTemplate.opsForValue().set(key, "revoked",
                    Objects.requireNonNull(Duration.ofMillis(remainingMilis)));
        }
    }

    public void revokeAllUserTokens(UUID userId) {
        revokedAllAccessTokens(userId);
        revokedAllRefreshTokens(userId);
    }

    public void checkTokenRevoked(String jti, String userId, Date issueAt) {
        if (jti == null && userId == null) {
            return;
        }
        if (jti != null) {
            checkBlacklist(jti);
        }
        if (userId != null) {
            checkRevokeAll(userId, issueAt);
        }
    }

    private RefreshTokenEntity toCreate(UserEntity user, String token) {
        OffsetDateTime expiresAt = OffsetDateTime.now().plus(authProperties.refreshTokenDays(), ChronoUnit.DAYS);
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUser(user);
        entity.setTokenHash(sha256hex(token));
        entity.setExpiresAt(expiresAt);
        return entity;
    }

    private void checkBlacklist(String jti) {
        if (jti == null) {
            return;
        }
        Boolean isJtiBlacklisted = stringRedisTemplate.hasKey(BLACKLIST_JTI_PREFIX + jti);
        if (Boolean.TRUE.equals(isJtiBlacklisted)) {
            throw AuthException.tokenRevoked();
        }
    }

    private void checkRevokeAll(String userId, Date issueAt) {
        if (userId == null) {
            return;
        }
        String value = stringRedisTemplate.opsForValue().get(REVOKE_ALL_USER_PREFIX + userId);
        if (value != null && issueAt != null) {
            long revokedAtEpoch = Long.parseLong(value);
            long issuedAtEpoch = issueAt.getTime() / 1000;
            if (issuedAtEpoch <= revokedAtEpoch) {
                throw AuthException.tokenRevoked();
            }
        }
    }

    private void revokedAllAccessTokens(UUID userId) {
        long currentEpochSeconds = Instant.now().getEpochSecond();
        String key = REVOKE_ALL_USER_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(key,
                Objects.requireNonNull(String.valueOf(currentEpochSeconds)),
                Objects.requireNonNull(Duration.ofMinutes(authProperties.accessTokenMinutes())));
    }

    private void revokedAllRefreshTokens(UUID userId) {
        List<RefreshTokenEntity> refreshTokens = tokenRepository.findAllByUserIdAndRevokedAtIsNull(userId);
        if (!refreshTokens.isEmpty()) {
            OffsetDateTime now = OffsetDateTime.now();
            refreshTokens.forEach(t -> t.setRevokedAt(now));
            tokenRepository.saveAll(refreshTokens);
        }
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
