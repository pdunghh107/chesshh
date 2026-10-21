package com.pdunghh.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdunghh.auth.dto.request.LoginRequest;
import com.pdunghh.auth.dto.request.RegisterRequest;
import com.pdunghh.auth.dto.request.UpdateMeRequest;
import com.pdunghh.auth.dto.response.LoginResponse;
import com.pdunghh.auth.dto.response.UserResponse;
import com.pdunghh.auth.entity.OutboxEvent;
import com.pdunghh.auth.entity.User;
import com.pdunghh.auth.repository.OutboxEventRepository;
import com.pdunghh.auth.repository.UserRepository;
import com.pdunghh.auth.service.TokenService;
import com.pdunghh.shared.api.ApiErrorCode;
import com.pdunghh.shared.exception.BusinessException;
import com.pdunghh.shared.security.RequestContext;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final long JWT_EXPIRATION_MS = 900000L;
    private static final long REFRESH_EXPIRATION_MS = 604800000L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtExpirationMs", JWT_EXPIRATION_MS);
        ReflectionTestUtils.setField(authService, "refreshExpirationMs", REFRESH_EXPIRATION_MS);
    }

    @AfterEach
    void tearDown() {
        RequestContext.clear();
    }

    private User createSampleUser(UUID id, String username, String email, boolean isActive) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .passwordHash("encodedPassword")
                .role("USER")
                .isActive(isActive)
                .build();
    }

    // ==========================================
    // 1. REGISTER TESTS
    // ==========================================
    @Nested
    @DisplayName("register() Tests")
    class RegisterTests {

        @Test
        @DisplayName("Đăng ký thành công - Lưu User, tạo OutboxEvent, trả về LoginResponse")
        void register_Success() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("john_doe", "john@example.com", "Password@123");
            UUID userId = UUID.randomUUID();
            User savedUser = createSampleUser(userId, request.username(), request.email(), true);

            when(userRepository.existsByEmail(request.email())).thenReturn(false);
            when(userRepository.existsByUsername(request.username())).thenReturn(false);
            when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"userId\":\"" + userId + "\"}");

            LoginResponse expectedResponse = new LoginResponse("access.token", "refresh.token",
                    UserResponse.of(savedUser));
            when(tokenService.buildLoginResponse(savedUser)).thenReturn(expectedResponse);

            // Act
            LoginResponse response = authService.register(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("access.token");
            assertThat(response.user().username()).isEqualTo("john_doe");

            verify(userRepository).save(any(User.class));

            ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepository).save(outboxCaptor.capture());
            OutboxEvent capturedEvent = outboxCaptor.getValue();
            assertThat(capturedEvent.getAggregateType()).isEqualTo("USER");
            assertThat(capturedEvent.getAggregateId()).isEqualTo(userId.toString());
            assertThat(capturedEvent.getEventType()).isEqualTo("USER_REGISTERED");
            assertThat(capturedEvent.getStatus()).isEqualTo("PENDING");
            assertThat(capturedEvent.getRetryCount()).isZero();
        }

        @Test
        @DisplayName("Đăng ký thất bại khi email đã tồn tại")
        void register_Fail_EmailAlreadyExists() {
            // Arrange
            RegisterRequest request = new RegisterRequest("john_doe", "john@example.com", "Password@123");
            when(userRepository.existsByEmail(request.email())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.code()).isEqualTo(ApiErrorCode.CONFLICT);
                        assertThat(be.getMessage()).isEqualTo("Email đã được sử dụng");
                    });

            verify(userRepository, never()).save(any(User.class));
            verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
        }

        @Test
        @DisplayName("Đăng ký thất bại khi username đã tồn tại")
        void register_Fail_UsernameAlreadyExists() {
            // Arrange
            RegisterRequest request = new RegisterRequest("john_doe", "john@example.com", "Password@123");
            when(userRepository.existsByEmail(request.email())).thenReturn(false);
            when(userRepository.existsByUsername(request.username())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.code()).isEqualTo(ApiErrorCode.CONFLICT);
                        assertThat(be.getMessage()).isEqualTo("Username đã được sử dụng");
                    });

            verify(userRepository, never()).save(any(User.class));
            verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
        }

        @Test
        @DisplayName("Đăng ký thất bại khi tuần tự hóa payload outbox bị lỗi")
        void register_Fail_SerializationError() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("john_doe", "john@example.com", "Password@123");
            User savedUser = createSampleUser(UUID.randomUUID(), request.username(), request.email(), true);

            when(userRepository.existsByEmail(request.email())).thenReturn(false);
            when(userRepository.existsByUsername(request.username())).thenReturn(false);
            when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Serialization failed") {
            });

            // Act & Assert
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Lỗi hệ thống khi khởi tạo sự kiện tài khoản");

            verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
            verify(tokenService, never()).buildLoginResponse(any(User.class));
        }
    }

    // ==========================================
    // 2. LOGIN TESTS
    // ==========================================
    @Nested
    @DisplayName("login() Tests")
    class LoginTests {

        @Test
        @DisplayName("Đăng nhập thành công - Đúng thông tin và tài khoản active")
        void login_Success() {
            // Arrange
            LoginRequest request = new LoginRequest("john@example.com", "Password@123");
            User user = createSampleUser(UUID.randomUUID(), "john_doe", request.email(), true);

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);

            LoginResponse expectedResponse = new LoginResponse("access.token", "refresh.token", UserResponse.of(user));
            when(tokenService.buildLoginResponse(user)).thenReturn(expectedResponse);

            // Act
            LoginResponse response = authService.login(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("access.token");
            verify(tokenService).buildLoginResponse(user);
        }

        @Test
        @DisplayName("Đăng nhập thất bại khi email không tồn tại")
        void login_Fail_EmailNotFound() {
            // Arrange
            LoginRequest request = new LoginRequest("unknown@example.com", "Password@123");
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.code()).isEqualTo(ApiErrorCode.UNAUTHORIZED);
                        assertThat(be.getMessage()).isEqualTo("Tài khoản hoặc mật khẩu không chính xác");
                    });
        }

        @Test
        @DisplayName("Đăng nhập thất bại khi sai mật khẩu")
        void login_Fail_InvalidPassword() {
            // Arrange
            LoginRequest request = new LoginRequest("john@example.com", "WrongPassword");
            User user = createSampleUser(UUID.randomUUID(), "john_doe", request.email(), true);

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.code()).isEqualTo(ApiErrorCode.UNAUTHORIZED);
                    });
        }

        @Test
        @DisplayName("Đăng nhập thất bại khi tài khoản bị vô hiệu hóa (isActive = false)")
        void login_Fail_AccountDeactivated() {
            // Arrange
            LoginRequest request = new LoginRequest("john@example.com", "Password@123");
            User user = createSampleUser(UUID.randomUUID(), "john_doe", request.email(), false);

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.code()).isEqualTo(ApiErrorCode.FORBIDDEN);
                        assertThat(be.getMessage()).isEqualTo("Tài khoản đã bị vô hiệu hóa");
                    });
        }
    }

    // ==========================================
    // 3. GET ME TESTS
    // ==========================================
    @Nested
    @DisplayName("getMe() Tests")
    class GetMeTests {

        @Test
        @DisplayName("Lấy thông tin bản thân thành công theo RequestContext")
        void getMe_Success() {
            // Arrange
            UUID userId = UUID.randomUUID();
            RequestContext.setUserId(userId);
            User user = createSampleUser(userId, "john_doe", "john@example.com", true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // Act
            UserResponse response = authService.getMe();

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(userId);
            assertThat(response.username()).isEqualTo("john_doe");
            assertThat(response.email()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("Lấy thông tin bản thân thất bại khi userId không tồn tại")
        void getMe_Fail_UserNotFound() {
            // Arrange
            UUID userId = UUID.randomUUID();
            RequestContext.setUserId(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.getMe())
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.code()).isEqualTo(ApiErrorCode.NOT_FOUND);
                    });
        }
    }

    // ==========================================
    // 4. UPDATE ME TESTS
    // ==========================================
    @Nested
    @DisplayName("updateMe() Tests")
    class UpdateMeTests {

        @Test
        @DisplayName("Cập nhật avatarUrl thành công")
        void updateMe_Success_WithAvatarUrl() {
            // Arrange
            UUID userId = UUID.randomUUID();
            RequestContext.setUserId(userId);
            User user = createSampleUser(userId, "john_doe", "john@example.com", true);
            UpdateMeRequest request = new UpdateMeRequest("https://cdn.example.com/new-avatar.png");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            // Act
            UserResponse response = authService.updateMe(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(user.getAvatarUrl()).isEqualTo("https://cdn.example.com/new-avatar.png");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Cập nhật khi avatarUrl null - giữ nguyên avatar cũ")
        void updateMe_Success_WithNullAvatarUrl() {
            // Arrange
            UUID userId = UUID.randomUUID();
            RequestContext.setUserId(userId);
            User user = createSampleUser(userId, "john_doe", "john@example.com", true);
            user.setAvatarUrl("https://cdn.example.com/old-avatar.png");
            UpdateMeRequest request = new UpdateMeRequest(null);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            // Act
            UserResponse response = authService.updateMe(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(user.getAvatarUrl()).isEqualTo("https://cdn.example.com/old-avatar.png");
            verify(userRepository).save(user);
        }
    }

    // ==========================================
    // 5. LOGOUT TESTS
    // ==========================================
    @Nested
    @DisplayName("logout() Tests")
    class LogoutTests {

        @Test
        @DisplayName("Đăng xuất thành công khi có cả JTI và refreshToken")
        void logout_Success_WithJtiAndRefreshToken() {
            // Arrange
            String jti = "test-jti-uuid";
            String refreshToken = "sample-refresh-token";
            RequestContext.setJti(jti);

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            // Act
            String result = authService.logout(refreshToken);

            // Assert
            assertThat(result).isEqualTo("Đăng xuất thành công");
            verify(valueOperations).set("blacklist:jti:" + jti, "revoked", JWT_EXPIRATION_MS, TimeUnit.MILLISECONDS);
            verify(tokenService).revokeRefreshTokenIfPresent(refreshToken);
        }

        @Test
        @DisplayName("Đăng xuất khi JTI null và refreshToken rỗng/khoảng trắng")
        void logout_Success_WithoutJtiAndBlankRefreshToken() {
            // Arrange
            RequestContext.setJti(null);

            // Act
            String result = authService.logout("   ");

            // Assert
            assertThat(result).isEqualTo("Đăng xuất thành công");
            verify(redisTemplate, never()).opsForValue();
            verify(tokenService, never()).revokeRefreshTokenIfPresent(anyString());
        }
    }

    // ==========================================
    // 6. LOGOUT ALL TESTS
    // ==========================================
    @Nested
    @DisplayName("logoutAll() Tests")
    class LogoutAllTests {

        @Test
        @DisplayName("Đăng xuất khỏi tất cả thiết bị - Ghi blacklist user vào Redis và revoke toàn bộ refresh token")
        void logoutAll_Success() {
            // Arrange
            UUID userId = UUID.randomUUID();
            RequestContext.setUserId(userId);
            User user = createSampleUser(userId, "john_doe", "john@example.com", true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            // Act
            String result = authService.logoutAll();

            // Assert
            assertThat(result).isEqualTo("Đăng xuất khỏi tất cả thiết bị thành công");
            verify(valueOperations).set("blacklist:user:" + userId, "revoked", REFRESH_EXPIRATION_MS,
                    TimeUnit.MILLISECONDS);
            verify(tokenService).revokeAllRefreshTokensByUser(user);
        }
    }

    // ==========================================
    // 7. REFRESH TOKEN TESTS
    // ==========================================
    @Nested
    @DisplayName("refreshToken() Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Làm mới token - Ủy quyền cho tokenService.refreshTokens")
        void refreshToken_Success() {
            // Arrange
            String refreshToken = "valid-refresh-token";
            User user = createSampleUser(UUID.randomUUID(), "john_doe", "john@example.com", true);
            LoginResponse expected = new LoginResponse("new.access.token", "new.refresh.token", UserResponse.of(user));

            when(tokenService.refreshTokens(refreshToken)).thenReturn(expected);

            // Act
            LoginResponse response = authService.refreshToken(refreshToken);

            // Assert
            assertThat(response).isEqualTo(expected);
            verify(tokenService).refreshTokens(refreshToken);
        }
    }

    // ==========================================
    // 8. DEACTIVATE ACCOUNT TESTS
    // ==========================================
    @Nested
    @DisplayName("deactivateAccount() Tests")
    class DeactivateAccountTests {

        @Test
        @DisplayName("Vô hiệu hóa tài khoản - isActive chuyển sang false, blacklist user và revoke refresh tokens")
        void deactivateAccount_Success() {
            // Arrange
            UUID userId = UUID.randomUUID();
            RequestContext.setUserId(userId);
            User user = createSampleUser(userId, "john_doe", "john@example.com", true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            // Act
            String result = authService.deactivateAccount();

            // Assert
            assertThat(result).isEqualTo("Vô hiệu hóa tài khoản thành công");
            assertThat(user.isActive()).isFalse();
            verify(userRepository).save(user);
            verify(valueOperations).set("blacklist:user:" + userId, "revoked", REFRESH_EXPIRATION_MS,
                    TimeUnit.MILLISECONDS);
            verify(tokenService).revokeAllRefreshTokensByUser(user);
        }
    }
}
