package com.pdunghh.auth.dto.request;

import com.pdunghh.auth.service.AuthService;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = AuthService.EMAIL_REQUIRED) @Email(message = AuthService.EMAIL_INVALID) String email,
        @NotBlank(message = AuthService.PASSWORD_REQUIRED) String password) {
}
