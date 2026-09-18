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

    public static BusinessException passwordNotMatch() {
        return new BusinessException(HttpStatus.BAD_REQUEST, "Xác nhận mật khẩu không khớp");
    }

    public static BusinessException invalidCredentials() {
        return new BusinessException(HttpStatus.BAD_REQUEST, "Email hoặc mật khẩu không chính xác");
    }

    public static BusinessException userInactive() {
        return new BusinessException(HttpStatus.FORBIDDEN, "Tài khoản đã bị vô hiệu hóa");
    }

    public static BusinessException userNotFound() {
        return new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản");
    }

    public static BusinessException invalidToken() {
        return new BusinessException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ");
    }

    public static BusinessException tokenExpired() {
        return new BusinessException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập đã hết hạn");
    }

    public static BusinessException tokenRevoked() {
        return new BusinessException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập đã bị thu hồi");
    }

}
