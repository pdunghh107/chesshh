package com.pdunghh.auth.dto.request;

import com.pdunghh.auth.service.AuthService;
import com.pdunghh.auth.validation.annotation.ValidFullName;
import com.pdunghh.auth.validation.annotation.ValidPhone;

import jakarta.validation.constraints.Email;

public record UpdateMeRequest(
        @ValidFullName String fullName,
        @Email(message = AuthService.EMAIL_INVALID) String email,
        @ValidPhone String phone) {
}