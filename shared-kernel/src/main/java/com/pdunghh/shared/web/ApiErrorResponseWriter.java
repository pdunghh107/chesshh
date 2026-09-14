package com.pdunghh.shared.web;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdunghh.shared.api.ApiError;
import com.pdunghh.shared.api.ApiErrorCode;
import com.pdunghh.shared.api.ErrorMessageSanitizer;
import com.pdunghh.shared.security.RequestContext;

import jakarta.servlet.http.HttpServletResponse;

public final class ApiErrorResponseWriter {
    private ApiErrorResponseWriter() {
    }

    public static void write(ObjectMapper objectMapper,
            String serviceName,
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String message,
            String path,
            List<String> details) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");

        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                ErrorMessageSanitizer.sanitizeMessage(message, ApiErrorCode.getDefaultErrorMessage(status.value())),
                path,
                RequestContext.getCorrelationId(),
                serviceName,
                details == null ? List.of() : ErrorMessageSanitizer.sanitizeDetails(details));

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
