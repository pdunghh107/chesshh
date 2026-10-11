package com.pdunghh.auth.controller;

import java.util.Objects;

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
import com.pdunghh.auth.service.AuthV2Service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthV2Controller {

    private final AuthV2Service authV2Service;

    @PostMapping("/register")
    public LoginResponse register(@RequestBody @Valid RegisterRequest request, HttpServletResponse response) {
        LoginResponse result = authV2Service.register(request);
        setRefreshTokenCookie(response, result.refreshToken());
        return result;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {
        LoginResponse result = authV2Service.login(request);
        setRefreshTokenCookie(response, result.refreshToken());
        return result;
    }

    @PostMapping("/logout")
    public String logout(@CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        String message = authV2Service.logout(refreshToken);
        clearRefreshTokenCookie(response);
        return message;
    }

    @PostMapping("/logout-all")
    public String logoutAll(HttpServletResponse response) {
        String message = authV2Service.logoutAll();
        clearRefreshTokenCookie(response);
        return message;
    }

    @PostMapping("/refresh-token")
    public LoginResponse refreshToken(@CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        checkRefreshToken(refreshToken);
        LoginResponse result = authV2Service.refreshToken(refreshToken);
        setRefreshTokenCookie(response, result.refreshToken());
        return result;
    }

    @GetMapping("/me")
    public UserResponse getMe() {
        return authV2Service.getMe();
    }

    @PutMapping("/me")
    public UserResponse updateMe(@RequestBody @Valid UpdateMeRequest request) {
        return authV2Service.updateMe(request);
    }

    @DeleteMapping("/me/deactivate")
    public String deactivateAccount() {
        authV2Service.deactivateAccount();
        return "Vô hiệu hóa tài khoản thành công (V2)";
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
