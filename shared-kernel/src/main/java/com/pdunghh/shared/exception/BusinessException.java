package com.pdunghh.shared.exception;

import java.util.List;

import com.pdunghh.shared.api.ApiErrorCode;
import com.pdunghh.shared.api.ApiErrorDetail;

public class BusinessException extends RuntimeException {

    private final ApiErrorCode code;
    private final List<ApiErrorDetail> errors;

    public BusinessException(ApiErrorCode code, String message, List<ApiErrorDetail> errors) {
        super(message);
        this.code = code;
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public BusinessException(ApiErrorCode code, String message) {
        this(code, message, List.of());
    }

    public BusinessException(ApiErrorCode code) {
        this(code, code.getMessage(), List.of());
    }

    public ApiErrorCode code() {
        return code;
    }

    public List<ApiErrorDetail> errors() {
        return errors;
    }

}
