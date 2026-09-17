package com.pdunghh.auth.exception;

import org.springframework.http.HttpStatus;

import com.pdunghh.shared.web.BusinessException;

public final class AuthException {
    private AuthException() {
    }

    private static final String EMAIL_TAKEN = "Email đã tồn tại";

    public static BusinessException emailTaken() {
        return new BusinessException(HttpStatus.CONFLICT, EMAIL_TAKEN);
    }

}
