package com.pdunghh.auth.exception;

import com.pdunghh.shared.api.ApiErrorCode;
import com.pdunghh.shared.exception.BusinessException;

public class AuthException {

    private AuthException() {}

    public static BusinessException userNotFound() {
        return new BusinessException(ApiErrorCode.NOT_FOUND, "Không tìm thấy người dùng");
    }

    public static BusinessException emailAlreadyExists() {
        return new BusinessException(ApiErrorCode.CONFLICT, "Email đã được sử dụng");
    }

    public static BusinessException usernameAlreadyExists() {
        return new BusinessException(ApiErrorCode.CONFLICT, "Username đã được sử dụng");
    }

    public static BusinessException invalidCredentials() {
        return new BusinessException(ApiErrorCode.UNAUTHORIZED, "Tài khoản hoặc mật khẩu không chính xác");
    }

    public static BusinessException accountDeactivated() {
        return new BusinessException(ApiErrorCode.FORBIDDEN, "Tài khoản đã bị vô hiệu hóa");
    }
}
