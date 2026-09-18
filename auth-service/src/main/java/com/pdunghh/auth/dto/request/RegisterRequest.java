package com.pdunghh.auth.dto.request;

import com.pdunghh.auth.service.AuthService;
import com.pdunghh.auth.validation.annotation.ValidFullName;
import com.pdunghh.auth.validation.annotation.ValidPassword;
import com.pdunghh.auth.validation.annotation.ValidPhone;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = AuthService.FULL_NAME_REQUIRED) @ValidFullName String fullName,
        @NotBlank(message = AuthService.EMAIL_REQUIRED) @Email(message = AuthService.EMAIL_INVALID) String email,
        @ValidPhone String phone,
        @NotBlank(message = AuthService.PASSWORD_REQUIRED) @ValidPassword String password,
        @NotBlank(message = AuthService.CONFIRM_PASSWORD_REQUIRED) String confirmPassword) {
}
