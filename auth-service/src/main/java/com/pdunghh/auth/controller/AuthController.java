package com.pdunghh.auth.controller;

import java.util.Objects;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pdunghh.auth.config.AuthProperties;
import com.pdunghh.auth.dto.request.ChangePasswordRequest;
import com.pdunghh.auth.dto.request.LoginRequest;
import com.pdunghh.auth.dto.request.RegisterRequest;
import com.pdunghh.auth.dto.request.UpdateMeRequest;
import com.pdunghh.auth.dto.response.LoginResponse;
import com.pdunghh.auth.dto.response.RefreshResponse;
import com.pdunghh.auth.dto.response.RegisterResponse;
import com.pdunghh.auth.dto.response.UserResponse;
import com.pdunghh.auth.security.AuthenticatedUser;
import com.pdunghh.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final AuthProperties authProperties;

    private final String COOKIE_NAME = "refresh_token";
    private final String LOGOUT_RESPONSE_MESSAGE = "Đăng xuất thành công";
    private final String LOGOUT_ALL_RESPONSE_MESSAGE = "Đăng xuất tất cả các thiết bị thành công";
    private final String CHANGE_PASSWORD_RESPONSE_MESSAGE = "Đổi mật khẩu thành công";
    private final String DEACTIVE_ACCOUNT_RESPONSE_MESSAGE = "Xóa tài khoản thành công";

    @PostMapping("/register")
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        RegisterResponse response = authService.register(request, httpRequest.getRemoteAddr());
        ResponseCookie cookie = setCookie(Objects.requireNonNull(response.refreshToken()));
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return response;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        LoginResponse response = authService.login(request);
        ResponseCookie cookie = setCookie(Objects.requireNonNull(response.refreshToken()));
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return response;
    }

    @GetMapping("/me")
    public UserResponse getMe(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        UserResponse response = authService.getMe(principal);
        return response;
    }

    @PutMapping("/me")
    public UserResponse updateMe(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody UpdateMeRequest request) {
        UserResponse response = authService.updateMe(principal, request);
        return response;
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(
            @CookieValue(name = COOKIE_NAME, required = false) String refreshToken,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        RefreshResponse response = authService.refresh(refreshToken, httpRequest.getRemoteAddr());
        ResponseCookie cookie = setCookie(Objects.requireNonNull(response.refreshToken()));
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return response;
    }

    @PutMapping("/change-password")
    public String changePassword(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletResponse httpResponse) {
        authService.changePassword(principal, request);
        ResponseCookie cookie = removeCookie();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return CHANGE_PASSWORD_RESPONSE_MESSAGE;
    }

    @PostMapping("/logout")
    public String logout(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @CookieValue(name = COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse httpResponse) {
        String accessToken = authHeader.substring(7);
        authService.logout(principal, accessToken, refreshToken);
        ResponseCookie cookie = removeCookie();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return LOGOUT_RESPONSE_MESSAGE;
    }

    @PostMapping("/logout-all")
    public String logoutAll(
            @AuthenticationPrincipal AuthenticatedUser principal,
            HttpServletResponse httpResponse) {
        authService.logoutAll(principal);
        ResponseCookie cookie = removeCookie();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return LOGOUT_ALL_RESPONSE_MESSAGE;
    }

    @PostMapping("/deactive")
    public String deactiveAccount(
            @AuthenticationPrincipal AuthenticatedUser principal,
            HttpServletResponse httpResponse) {
        authService.deactiveAccount(principal);
        ResponseCookie cookie = removeCookie();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return DEACTIVE_ACCOUNT_RESPONSE_MESSAGE;
    }

    private ResponseCookie setCookie(@NonNull String refreshToken) {
        long maxAgeInSeconds = authProperties.refreshTokenDays() * 24 * 60 * 60;
        return ResponseCookie.from(COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAgeInSeconds)
                .build();
    }

    private ResponseCookie removeCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
    }
}
