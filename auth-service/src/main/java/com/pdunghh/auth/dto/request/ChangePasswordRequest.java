package com.pdunghh.auth.dto.request;

import com.pdunghh.auth.service.AuthService;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = AuthService.PASSWORD_REQUIRED) String oldPassword,
        @NotBlank(message = AuthService.PASSWORD_REQUIRED) String newPassword,
        @NotBlank(message = AuthService.CONFIRM_PASSWORD_REQUIRED) String confirmPassword) {

}
