package com.pdunghh.shared.web;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.pdunghh.shared.api.ApiError;
import com.pdunghh.shared.api.ApiErrorCode;
import com.pdunghh.shared.api.ApiErrorDetail;
import com.pdunghh.shared.api.ErrorMessageSanitizer;
import com.pdunghh.shared.security.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private final String serviceName;

    public GlobalExceptionHandler(
            @Value("${spring.application.name:unknown-service}") String serviceName) {
        this.serviceName = serviceName;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex, HttpServletRequest request) {
        logBusinessException(ex, request);
        return buildResponse(
                Objects.requireNonNull(ex.getStatus()),
                ex.getCode(),
                ex.getMessage(),
                request,
                ex.getDetails() == null ? List.of() : ErrorMessageSanitizer.sanitizeDetails(ex.getDetails()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        List<ApiErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(ApiErrorDetail::build)
                .toList();
        return buildResponse(status, request, details);
    }

    private ResponseEntity<ApiError> buildResponse(
            @NonNull HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            List<?> details) {

        ApiError body = ApiError.build(
                status,
                code,
                message,
                request,
                serviceName,
                details);

        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<ApiError> buildResponse(
            @NonNull HttpStatus status,
            HttpServletRequest request,
            List<?> details) {
        ApiError body = ApiError.build(
                status,
                ApiErrorCode.getDefaultErrorCode(status.value()),
                ApiErrorCode.getDefaultErrorMessage(status.value()),
                request,
                serviceName,
                details);

        return ResponseEntity.status(status).body(body);
    }

    private void logBusinessException(BusinessException ex, HttpServletRequest request) {
        if (ex.getStatus().is5xxServerError()) {
            log.error("[BUSINESS EXCEPTION] LOI {} TAI API {} TRACE {}: {}", ex.getCode(), request.getRequestURI(),
                    RequestContext.getCorrelationId(), ex.getMessage(), ex);
            return;
        }
        log.warn("[BUSINESS EXCEPTION] LOI {} TAI API {} TRACE {}: {}", ex.getCode(), request.getRequestURI(),
                RequestContext.getCorrelationId(), ex.getMessage());

    }

}
