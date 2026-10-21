package com.pdunghh.auth.controller;

import java.util.Objects;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pdunghh.auth.dto.request.LoginRequest;
import com.pdunghh.auth.dto.request.RegisterRequest;
import com.pdunghh.auth.dto.request.UpdateMeRequest;
import com.pdunghh.auth.dto.response.LoginResponse;
import com.pdunghh.auth.dto.response.UserResponse;
import com.pdunghh.auth.exception.AuthException;
import com.pdunghh.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Controller xử lý các yêu cầu xác thực, quản lý phiên và tài khoản người dùng.
 * <p>
 * Cung cấp các endpoint REST API cho việc đăng ký, đăng nhập, cấp mới token,
 * đăng xuất và quản lý thông tin tài khoản cá nhân.
 * </p>
 *
 * @author ChessHH Team
 * @see AuthService
 */
@Tag(name = "Authentication", description = "Các API xác thực danh tính, quản lý phiên và tài khoản người dùng")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Đăng ký tài khoản người dùng mới vào hệ thống.
     *
     * @param request  Thông tin đăng ký gồm tên đăng nhập, email và mật khẩu hợp
     *                 lệ.
     * @param response Đối tượng HttpServletResponse dùng để thiết lập HttpOnly
     *                 cookie cho refresh token.
     * @return {@link LoginResponse} chứa Access Token và thông tin cơ bản của người
     *         dùng.
     */
    @Operation(summary = "Đăng ký tài khoản", description = "Tạo tài khoản mới, kích hoạt outbox event và trả về cặp token")
    @PostMapping("/register")
    public LoginResponse register(@RequestBody @Valid RegisterRequest request, HttpServletResponse response) {
        LoginResponse result = authService.register(request);
        setRefreshTokenCookie(response, result.refreshToken());
        return result;
    }

    /**
     * Xác thực thông tin đăng nhập và tạo phiên làm việc mới.
     *
     * @param request  Thông tin đăng nhập gồm email và mật khẩu.
     * @param response Đối tượng HttpServletResponse dùng để thiết lập HttpOnly
     *                 cookie cho refresh token.
     * @return {@link LoginResponse} chứa Access Token và thông tin người dùng.
     */
    @Operation(summary = "Đăng nhập", description = "Kiểm tra thông tin đăng nhập và cấp mới Access Token cùng Refresh Token")
    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {
        LoginResponse result = authService.login(request);
        setRefreshTokenCookie(response, result.refreshToken());
        return result;
    }

    /**
     * Đăng xuất phiên làm việc hiện tại trên thiết bị này.
     *
     * @param refreshToken Token làm mới đọc từ HttpOnly Cookie (nếu có).
     * @param response     Đối tượng HttpServletResponse dùng để xoá cookie refresh
     *                     token.
     * @return Thông báo kết quả đăng xuất.
     */
    @Operation(summary = "Đăng xuất thiết bị hiện tại", description = "Đưa JTI vào Redis blacklist và thu hồi Refresh Token hiện tại")
    @PostMapping("/logout")
    public String logout(@CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        String message = authService.logout(refreshToken);
        clearRefreshTokenCookie(response);
        return message;
    }

    /**
     * Đăng xuất khỏi toàn bộ các thiết bị đang đăng nhập.
     *
     * @param response Đối tượng HttpServletResponse dùng để xoá cookie trên thiết
     *                 bị hiện tại.
     * @return Thông báo kết quả đăng xuất toàn bộ.
     */
    @Operation(summary = "Đăng xuất tất cả thiết bị", description = "Đưa User ID vào Redis blacklist và thu hồi toàn bộ Refresh Token của user trong database")
    @PostMapping("/logout-all")
    public String logoutAll(HttpServletResponse response) {
        String message = authService.logoutAll();
        clearRefreshTokenCookie(response);
        return message;
    }

    @PostMapping("/refresh-token")
    public LoginResponse refreshToken(@CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        checkRefreshToken(refreshToken);
        LoginResponse result = authService.refreshToken(refreshToken);
        setRefreshTokenCookie(response, result.refreshToken());
        return result;
    }

    @GetMapping("/me")
    public UserResponse getMe() {
        return authService.getMe();
    }

    @PutMapping("/me")
    public UserResponse updateMe(@RequestBody @Valid UpdateMeRequest request) {
        return authService.updateMe(request);
    }

    @DeleteMapping("/me/deactivate")
    public String deactivateAccount(HttpServletResponse response) {
        String message = authService.deactivateAccount();
        clearRefreshTokenCookie(response);
        return message;
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", Objects.requireNonNull(refreshToken))
                .httpOnly(true)
                .secure(false) // TODO: Set to true if running over HTTPS in Production
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void checkRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw AuthException.invalidCredentials();
        }
    }
}
