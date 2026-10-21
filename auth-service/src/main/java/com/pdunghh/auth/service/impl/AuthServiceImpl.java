package com.pdunghh.auth.service.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
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
        checkRegisterDataValid(request);
        User user = saveRegisterData(request);
        handleUserRegisteredOutboxEvent(user);
        return tokenService.buildLoginResponse(user);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = findUserByEmail(request.email());
        checkLoginCredentials(user, request);
        return tokenService.buildLoginResponse(user);
    }

    @Override
    public UserResponse getMe() {
        UUID userId = RequestContext.getUserId();
        User user = findUserById(Objects.requireNonNull(userId));
        return UserResponse.of(user);
    }

    @Override
    @Transactional
    public UserResponse updateMe(UpdateMeRequest request) {
        UUID userId = RequestContext.getUserId();
        User user = findUserById(Objects.requireNonNull(userId));
        user = saveUpdateData(user, request);
        return UserResponse.of(user);
    }

    @Override
    @Transactional
    public String logout(String refreshToken) {
        String jti = RequestContext.getJti();
        writeJtiBlacklistKey(jti);
        revokeRefreshTokenIfPresent(refreshToken);
        return "Đăng xuất thành công";
    }

    @Override
    @Transactional
    public String logoutAll() {
        UUID userId = RequestContext.getUserId();
        User user = findUserById(Objects.requireNonNull(userId));
        revokeAllTokensByUser(user);
        return "Đăng xuất khỏi tất cả thiết bị thành công";
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        return tokenService.refreshTokens(refreshToken);
    }

    @Override
    @Transactional
    public String deactivateAccount() {
        UUID userId = RequestContext.getUserId();
        User user = findUserById(Objects.requireNonNull(userId));
        saveDeactiveData(user);
        revokeAllTokensByUser(user);
        return "Vô hiệu hóa tài khoản thành công";
    }

    // [UTILS]

    // [OUT EXCEPTION]
    private void checkEmailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw AuthException.emailAlreadyExists();
        }
    }

    private void checkUsernameExists(String username) {
        if (userRepository.existsByUsername(username)) {
            throw AuthException.usernameAlreadyExists();
        }
    }

    private void checkPasswordMatches(String password, String passwordHash) {
        if (!passwordEncoder.matches(password, passwordHash)) {
            throw AuthException.invalidCredentials();
        }
    }

    private void checkUserActive(User user) {
        if (!user.isActive()) {
            throw AuthException.accountDeactivated();
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(AuthException::invalidCredentials);
    }

    private User findUserById(@NonNull UUID userId) {
        return userRepository.findById(userId).orElseThrow(AuthException::userNotFound);
    }

    // [PRE HANDLE]
    private void checkRegisterDataValid(RegisterRequest request) {
        checkEmailExists(request.email());
        checkUsernameExists(request.username());
    }

    private void checkLoginCredentials(User user, LoginRequest request) {
        checkPasswordMatches(request.password(), user.getPasswordHash());
        checkUserActive(user);
    }

    private String generateUserRegisterEventPayload(User user) throws JsonProcessingException {
        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                Instant.now());

        return objectMapper.writeValueAsString(event);
    }

    // [SAVE TO DB]
    private User saveRegisterData(RegisterRequest request) {
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role("USER")
                .isActive(true)
                .build();

        return userRepository.save(Objects.requireNonNull(user));
    }

    private User saveUpdateData(User user, UpdateMeRequest request) {
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        return userRepository.save(Objects.requireNonNull(user));
    }

    private void saveDeactiveData(User user) {
        user.setActive(false);
        userRepository.save(user);
    }

    private void saveUserRegisterOutboxEvent(User user, String payload) {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType("USER")
                .aggregateId(user.getId().toString())
                .eventType("USER_REGISTERED")
                .payload(payload)
                .status("PENDING")
                .retryCount(0)
                .build();

        outboxEventRepository.save(Objects.requireNonNull(outboxEvent));
    }

    // [POST HANDLE, SIDE EFFECTS]
    private void writeUserIdBlacklistKey(String userIdStr) {
        redisTemplate.opsForValue().set("blacklist:user:" + userIdStr, "revoked", refreshExpirationMs,
                TimeUnit.MILLISECONDS);
    }

    private void writeJtiBlacklistKey(String jti) {
        if (StringUtils.hasText(jti)) {
            redisTemplate.opsForValue().set("blacklist:jti:" + jti, "revoked", jwtExpirationMs,
                    TimeUnit.MILLISECONDS);
        }
    }

    private void revokeAllTokensByUser(User user) {
        writeUserIdBlacklistKey(user.getId().toString());
        tokenService.revokeAllRefreshTokensByUser(user);
    }

    private void revokeRefreshTokenIfPresent(String refreshToken) {
        if (StringUtils.hasText(refreshToken)) {
            tokenService.revokeRefreshTokenIfPresent(refreshToken);
        }
    }

    private void handleUserRegisteredOutboxEvent(User user) {
        try {
            String payload = generateUserRegisterEventPayload(user);
            saveUserRegisterOutboxEvent(user, payload);
        } catch (Exception e) {
            // TODO : JSON Processing Exceptional handler
            log.error("Failed to serialize and save OutboxEvent for userId: {}", user.getId(), e);
            throw new RuntimeException("Lỗi hệ thống khi khởi tạo sự kiện tài khoản", e);
        }
    }

}
