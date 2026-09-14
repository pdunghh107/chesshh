package com.pdunghh.shared.web;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.pdunghh.shared.api.ApiErrorCode;

public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final List<Object> details;

    public BusinessException(HttpStatus status, String code, String message, List<?> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public BusinessException(HttpStatus status) {
        this(status,
                ApiErrorCode.getDefaultErrorCode(status.value()),
                ApiErrorCode.getDefaultErrorMessage(status.value()),
                List.of());
    }

    public BusinessException(HttpStatus status, ApiErrorCode code) {
        this(status, code.getValue(), ApiErrorCode.getDefaultErrorMessage(status.value()), List.of());
    }

    public BusinessException(HttpStatus status, String message) {
        this(status, ApiErrorCode.getDefaultErrorCode(status.value()), message, List.of());
    }

    public BusinessException(HttpStatus status, List<?> details) {
        this(status,
                ApiErrorCode.getDefaultErrorCode(status.value()),
                ApiErrorCode.getDefaultErrorMessage(status.value()),
                details);
    }

    public BusinessException(HttpStatus status, ApiErrorCode code, List<?> details) {
        this(status, code.getValue(), ApiErrorCode.getDefaultErrorMessage(status.value()), details);
    }

    public BusinessException(HttpStatus status, String message, List<?> details) {
        this(status, ApiErrorCode.getDefaultErrorCode(status.value()), message, details);
    }

    public BusinessException(HttpStatus status, ApiErrorCode code, String message) {
        this(status, code.getValue(), message, List.of());
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public List<Object> getDetails() {
        return details;
    }

}
