package com.pdunghh.auth.service;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pdunghh.auth.dto.request.ChangePasswordRequest;
import com.pdunghh.auth.dto.request.LoginRequest;
import com.pdunghh.auth.dto.request.RegisterRequest;
import com.pdunghh.auth.dto.request.UpdateMeRequest;
import com.pdunghh.auth.dto.response.LoginResponse;
import com.pdunghh.auth.dto.response.RefreshResponse;
import com.pdunghh.auth.dto.response.RegisterResponse;
import com.pdunghh.auth.dto.response.UserResponse;
import com.pdunghh.auth.entity.RefreshTokenEntity;
import com.pdunghh.auth.entity.UserEntity;
import com.pdunghh.auth.exception.AuthException;
import com.pdunghh.auth.repository.UserRepository;
import com.pdunghh.auth.security.AuthenticatedUser;
import com.pdunghh.shared.utils.SharedUtils;

import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // validate string
    public static final String FULL_NAME_REQUIRED = "Họ tên không được để trống";
    public static final String FULL_NAME_INVALID = "Họ tên không hợp lệ";
    public static final String EMAIL_REQUIRED = "Email không được để trống";
    public static final String EMAIL_INVALID = "Email không hợp lệ";
    public static final String PHONE_INVALID = "Số điện thoại không hợp lệ";
    public static final String PASSWORD_REQUIRED = "Mật khẩu không được để trống";
    public static final String PASSWORD_INVALID = "Mật khẩu không hợp lệ";
    public static final String CONFIRM_PASSWORD_REQUIRED = "Xác nhận mật khẩu không được để trống";

    public static final String OLD_PASSWORD_REQUIRED = "Mật khẩu cũ không được để trống";
    public static final String NEW_PASSWORD_REQUIRED = "Mật khẩu mới không được để trống";

    private static final String REFRESH_TOKEN_NOT_FOUND = "Refresh token không tồn tại";

    public static final String ROLE_USER = "user";

    @Transactional
    public RegisterResponse register(RegisterRequest request, String ipAddress) {
        checkEmailExisted(request.email());
        checkConfirmPasswordMatch(request.password(), request.confirmPassword());
        UserEntity user = toCreate(request);
        user = userRepository.save(Objects.requireNonNull(user));
        TokenPair tokens = createTokens(user);
        return RegisterResponse.from(tokens.accessToken(), tokens.refreshToken(), user);
    }

    // TODO : handle case login failed to many times
    public LoginResponse login(LoginRequest request) {
        UserEntity user = findByEmail(request.email());
        checkPasswordMatch(request.password(), user.getPasswordHash());
        checkUserActive(user);
        toLogin(user);
        user = userRepository.save(Objects.requireNonNull(user));
        TokenPair tokens = createTokens(user);
        return LoginResponse.from(tokens.accessToken(), tokens.refreshToken(), user);
    }

    public UserResponse getMe(AuthenticatedUser principal) {
        UserEntity user = findUser(Objects.requireNonNull(principal.userId()));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateMe(AuthenticatedUser principal, UpdateMeRequest request) {
        UserEntity user = findUser(Objects.requireNonNull(principal.userId()));
        toUpdateFieldNeeded(user, request);
        return UserResponse.from(user);
    }

    @Transactional
    public RefreshResponse refresh(String refreshToken, String ipAddress) {
        RefreshTokenEntity token = jwtService.findRefreshToken(refreshToken);
        checkTokenValid(token);
        token.setRevokedAt(OffsetDateTime.now());
        TokenPair tokens = createTokens(token.getUser());
        return RefreshResponse.from(tokens.accessToken(), tokens.refreshToken());
    }

    @Transactional
    public void changePassword(AuthenticatedUser principal, ChangePasswordRequest request) {
        UserEntity user = findUser(Objects.requireNonNull(principal.userId()));
        checkPasswordMatch(request.oldPassword(), user.getPasswordHash());
        checkConfirmPasswordMatch(request.newPassword(), request.confirmPassword());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        jwtService.revokeAllUserTokens(user.getId());
    }

    @Transactional
    public void deactiveAccount(AuthenticatedUser principal) {
        UserEntity user = findUser(Objects.requireNonNull(principal.userId()));
        user.setActive(false);
        jwtService.revokeAllUserTokens(user.getId());
    }

    // TODO : change avatar call s3

    @Transactional
    public void logout(AuthenticatedUser principal, String accessToken, String refreshToken) {
        UserEntity user = findUser(Objects.requireNonNull(principal.userId()));
        handleRevokedRefreshToken(user, refreshToken);
        Claims claims = jwtService.parse(accessToken);
        jwtService.revokeTokenByJti(claims.getId(), claims.getExpiration());
    }

    @Transactional
    public void logoutAll(AuthenticatedUser principal) {
        UserEntity user = findUser(Objects.requireNonNull(principal.userId()));
        jwtService.revokeAllUserTokens(user.getId());
    }

    // [COMMON]
    private UserEntity findUser(@NonNull UUID id) {
        return userRepository.findById(id).orElseThrow(AuthException::userNotFound);
    }

    private void checkConfirmPasswordMatch(String password, String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) {
            throw AuthException.passwordNotMatch();
        }
    }

    private record TokenPair(String accessToken, String refreshToken) {
    }

    private TokenPair createTokens(UserEntity user) {
        String accessToken = jwtService.createAccessToken(user, ROLE_USER);
        String refreshToken = jwtService.createRefresh(user);
        return new TokenPair(accessToken, refreshToken);
    }

    // [REGISTER]
    private void checkEmailExisted(String email) {
        String emailTrim = SharedUtils.normalize(email);
        userRepository.findByEmailIgnoreCase(emailTrim).ifPresent(u -> {
            throw AuthException.emailTaken();
        });
    }

    private UserEntity toCreate(RegisterRequest request) {
        UserEntity user = new UserEntity();
        user.setFullName(request.fullName());
        user.setEmail(SharedUtils.normalize(request.email()));
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setLastLoginAt(OffsetDateTime.now());
        user.setRole(ROLE_USER);
        return user;
    }

    // [LOGIN]
    private void toLogin(UserEntity user) {
        user.setLastLoginAt(OffsetDateTime.now());
    }

    private UserEntity findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(SharedUtils.normalize(email))
                .orElseThrow(AuthException::invalidCredentials);
    }

    private void checkPasswordMatch(String password, String passwordHash) {
        if (password == null) {
            return;
        }
        if (!passwordEncoder.matches(password, passwordHash)) {
            throw AuthException.invalidCredentials();
        }
    }

    private void checkUserActive(UserEntity user) {
        if (!user.isActive()) {
            throw AuthException.userInactive();
        }
    }

    // [UPDATE]
    private void toUpdateFieldNeeded(UserEntity user, UpdateMeRequest request) {
        SharedUtils.shouldUpdate(request.fullName(), user.getFullName(), user::setFullName, true);

        if (StringUtils.hasText(request.email())) {
            String newEmail = SharedUtils.normalize(request.email());
            if (!newEmail.equals(user.getEmail())) {
                checkEmailExisted(newEmail);
                user.setEmail(newEmail);
            }
        }
        SharedUtils.shouldUpdate(request.phone(), user.getPhone(), user::setPhone, false);
    }

    // [REFRESH]
    private void checkTokenValid(RefreshTokenEntity token) {
        if (token == null) {
            return;
        }
        if (token.getRevokedAt() != null) {
            jwtService.revokeAllUserTokens(token.getUser().getId());
            throw AuthException.tokenRevoked();
        }
        if (token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw AuthException.tokenExpired();
        }
    }

    // write login history

    // set config

    // send email

    // [LOGOUT]
    private void validateRefreshToken(UUID userId, UUID tokenUserId) {
        if (userId == null || tokenUserId == null || !userId.equals(tokenUserId)) {
            throw AuthException.invalidToken();
        }
    }

    private void handleRevokedRefreshToken(UserEntity user, String refreshToken) {
        if (StringUtils.hasText(refreshToken)) {
            try {
                RefreshTokenEntity token = jwtService.findRefreshToken(refreshToken);
                validateRefreshToken(user.getId(), token.getUser().getId());
                token.setRevokedAt(OffsetDateTime.now());
            } catch (Exception e) {
                log.warn(REFRESH_TOKEN_NOT_FOUND, e);
            }
        }
    }
}
