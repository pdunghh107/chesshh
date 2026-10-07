package com.pdunghh.shared.api;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String traceId,
        Instant timestamp,
        int status,
        String code,
        String message,
        T data) {

    public static <T> ApiResponse<T> success(String traceId, int status, String code, String message, T data) {
        return new ApiResponse<>(
                true,
                traceId,
                Instant.now(),
                status,
                code,
                message,
                data);
    }

    public static <T> ApiResponse<T> ok(String traceId, ApiSuccessCode code, T data) {
        return success(traceId, 200, code.getCode(), code.getMessage(), data);
    }

    public static <T> ApiResponse<T> created(String traceId, ApiSuccessCode code, T data) {
        return success(traceId, 201, code.getCode(), code.getMessage(), data);
    }

    public static <T> ApiResponse<PageResponse<T>> paged(String traceId, ApiSuccessCode code, PageResponse<T> page) {
        return success(traceId, 200, code.getCode(), code.getMessage(), page);
    }

}