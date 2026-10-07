package com.pdunghh.shared.api;

import java.time.Instant;
import java.util.List;

public record ApiError(
        boolean success,
        String traceId,
        Instant timestamp,
        int status,
        String code,
        String message,
        String serviceName,
        String path,
        List<ApiErrorDetail> errors) {

    public static ApiError of(
            String traceId,
            int status,
            String code,
            String message,
            String serviceName,
            String path,
            List<ApiErrorDetail> errors) {

        return new ApiError(
                false,
                traceId,
                Instant.now(),
                status,
                code,
                message,
                serviceName,
                path,
                errors);
    }
}