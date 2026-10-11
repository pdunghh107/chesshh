package com.pdunghh.shared.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("jwtAuthFilter")
@RequiredArgsConstructor
@Slf4j
public class RequestContextFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Value("${app.jwt.secret:default-secret-key-that-should-be-changed-in-production}")
    private String jwtSecret;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Setup TraceId for tracking request
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString();
        }
        RequestContext.setTraceId(traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        try {
            // 2. Extract and Parse JWT Token
            String token = extractToken(request);
            if (StringUtils.hasText(token)) {
                Claims claims = parseToken(token);
                if (claims != null && claims.getSubject() != null) {
                    // Check Redis for revoked token
                    if (isTokenRevoked(claims)) {
                        String uri = request.getRequestURI();
                        // TODO : cau hinh white list vao trong nay
                        if (uri.endsWith("/logout") || uri.endsWith("/logout-all")) {
                            log.debug("Token is revoked but allowing logout request to pass: {}", uri);
                        } else {
                            throw new RuntimeException("Token has been revoked");
                        }
                    }

                    try {
                        UUID userId = UUID.fromString(claims.getSubject());
                        RequestContext.setUserId(userId);
                        RequestContext.setJti(claims.getId());
                        log.debug("Authenticated user: {} for request: {}", userId, request.getRequestURI());
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid userId format in token subject: {}", claims.getSubject());
                    }
                }
            }

            // 3. Continue filter chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error in RequestContextFilter", e);
            // Có thể throw Exception hoặc write response trực tiếp nếu muốn đóng vai trò là
            // Gateway chặn request
            throw new RuntimeException("Unauthorized / Error processing token", e);
        } finally {
            // 4. Clean up Context to prevent memory leak in ThreadLocal
            RequestContext.clear();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private boolean isTokenRevoked(Claims claims) {
        String jti = claims.getId();
        if (StringUtils.hasText(jti)) {
            Boolean hasKey = redisTemplate.hasKey("blacklist:jti:" + jti);
            if (Boolean.TRUE.equals(hasKey)) {
                log.warn("Token jti {} is revoked", jti);
                return true;
            }
        }

        String userId = claims.getSubject();
        if (StringUtils.hasText(userId)) {
            Boolean hasKey = redisTemplate.hasKey("blacklist:user:" + userId);
            if (Boolean.TRUE.equals(hasKey)) {
                log.warn("All tokens for user {} are revoked", userId);
                return true;
            }
        }
        return false;
    }

    private Claims parseToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Invalid JWT Token: {}", e.getMessage());
            return null;
        }
    }
}
