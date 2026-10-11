package com.pdunghh.auth.service.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdunghh.auth.dto.request.LoginRequest;
import com.pdunghh.auth.dto.request.RegisterRequest;
import com.pdunghh.auth.dto.request.UpdateMeRequest;
import com.pdunghh.auth.dto.response.LoginResponse;
import com.pdunghh.auth.dto.response.UserResponse;
import com.pdunghh.auth.entity.OutboxEvent;
import com.pdunghh.auth.entity.User;
import com.pdunghh.auth.exception.AuthException;
import com.pdunghh.auth.repository.OutboxEventRepository;
import com.pdunghh.auth.repository.UserRepository;
import com.pdunghh.auth.service.AuthService;
import com.pdunghh.auth.service.TokenService;
import com.pdunghh.shared.event.UserRegisteredEvent;
import com.pdunghh.shared.security.RequestContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        log.info("Register new user: {}", request.email());
        registerValidate(request);
        User user = registerSaveToDb(request);

        saveUserRegisteredOutboxEvent(user);

        return tokenService.generateLoginResponse(user);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login user: {}", request.email());

        User user = findUserByEmail(request.email());
        checkUserAccount(user, request);
        return tokenService.generateLoginResponse(user);
    }

    @Override
    public UserResponse getMe() {
        UUID userId = RequestContext.getUserId();
        log.info("Get profile for user: {}", userId);
        User user = findUserById(userId);
        return UserResponse.fromUser(user);
    }

    @Override
    @Transactional
    public UserResponse updateMe(UpdateMeRequest request) {
        UUID userId = RequestContext.getUserId();
        log.info("Update profile for user: {}", userId);
        User user = findUserById(userId);

        user.setAvatarUrl(request.avatarUrl());
        user = userRepository.save(user);

        return UserResponse.fromUser(user);
    }

    @Override
    @Transactional
    public String logout(String refreshToken) {
        String jti = RequestContext.getJti();
        log.info("Logout for jti: {}", jti);

        if (jti != null) {
            redisTemplate.opsForValue().set("blacklist:jti:" + jti, "revoked", jwtExpirationMs, TimeUnit.MILLISECONDS);
        }

        if (refreshToken != null && !refreshToken.isBlank()) {
            tokenService.revokeToken(refreshToken);
        }

        return "Đăng xuất thành công";
    }

    @Override
    @Transactional
    public String logoutAll() {
        UUID userId = RequestContext.getUserId();
        log.info("Logout all for user: {}", userId);
        User user = findUserById(userId);

        logoutAllUserTokens(user);
        return "Đăng xuất khỏi tất cả thiết bị thành công";
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        log.info("Refresh token attempt");
        return tokenService.refreshTokens(refreshToken);
    }

    @Override
    @Transactional
    public void deactivateAccount() {
        UUID userId = RequestContext.getUserId();
        log.info("Deactivate account for user: {}", userId);
        User user = findUserById(userId);
        deactiveSaveToDb(user);
        logoutAllUserTokens(user);
    }

    // [UTILS]
    private void registerValidate(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw AuthException.emailAlreadyExists();
        }

        if (userRepository.existsByUsername(request.username())) {
            throw AuthException.usernameAlreadyExists();
        }
    }

    private User registerSaveToDb(RegisterRequest request) {
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role("USER")
                .isActive(true)
                .build();

        return userRepository.save(Objects.requireNonNull(user));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(AuthException::userNotFound);
    }

    private void checkUserAccount(User user, LoginRequest request) {
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw AuthException.invalidCredentials();
        }

        if (!user.isActive()) {
            throw AuthException.accountDeactivated();
        }
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(Objects.requireNonNull(userId)).orElseThrow(AuthException::userNotFound);
    }

    private void deactiveSaveToDb(User user) {
        user.setActive(false);
        userRepository.save(user);
    }

    private void logoutAllUserTokens(User user) {
        String userIdStr = user.getId().toString();
        redisTemplate.opsForValue().set("blacklist:user:" + userIdStr, "revoked", refreshExpirationMs,
                TimeUnit.MILLISECONDS);
        tokenService.revokeAllTokens(user);
    }

    private void saveUserRegisteredOutboxEvent(User user) {
        try {
            UserRegisteredEvent event = new UserRegisteredEvent(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    Instant.now());
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType("USER")
                    .aggregateId(user.getId().toString())
                    .eventType("USER_REGISTERED")
                    .payload(payload)
                    .status("PENDING")
                    .retryCount(0)
                    .build();

            outboxEventRepository.save(Objects.requireNonNull(outboxEvent));
            log.info("OutboxEvent USER_REGISTERED saved for userId: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to serialize and save OutboxEvent for userId: {}", user.getId(), e);
            throw new RuntimeException("Lỗi hệ thống khi khởi tạo sự kiện tài khoản", e);
        }
    }

}
