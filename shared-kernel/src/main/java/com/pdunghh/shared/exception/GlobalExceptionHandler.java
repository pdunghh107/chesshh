package com.pdunghh.shared.exception;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.pdunghh.shared.api.ApiError;
import com.pdunghh.shared.api.ApiErrorCode;
import com.pdunghh.shared.api.ApiErrorDetail;
import com.pdunghh.shared.api.ErrorMessageSanitizer;
import com.pdunghh.shared.security.RequestContext;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @Value("${spring.application.name:unknown-service}")
        private String serviceName;

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiError> handleBusiness(BusinessException ex, HttpServletRequest request) {
                return build(
                                ex.code().getStatus(),
                                ex.code().getCode(),
                                ex.getMessage(),
                                request,
                                sanitizeErrors(ex.errors()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                List<ApiErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> ApiErrorDetail.of(error.getField(), error.getCode(),
                                                error.getDefaultMessage()))
                                .toList();

                return build(
                                HttpStatus.BAD_REQUEST,
                                ApiErrorCode.FIELD_INVALID.getCode(),
                                ApiErrorCode.FIELD_INVALID.getMessage(),
                                request,
                                errors);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {

                List<ApiErrorDetail> errors = ex.getConstraintViolations().stream()
                                .map(error -> {
                                        String code = error.getConstraintDescriptor().getAnnotation().annotationType()
                                                        .getSimpleName();
                                        return ApiErrorDetail.of(error.getPropertyPath().toString(),
                                                        code,
                                                        error.getMessage());
                                })
                                .toList();

                return build(
                                HttpStatus.BAD_REQUEST,
                                ApiErrorCode.FIELD_INVALID.getCode(),
                                ApiErrorCode.FIELD_INVALID.getMessage(),
                                request,
                                errors);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
                return build(
                                HttpStatus.BAD_REQUEST,
                                ApiErrorCode.BAD_REQUEST.getCode(),
                                sanitizeMessage(ex.getMessage(), ApiErrorCode.BAD_REQUEST.getMessage()),
                                request,
                                List.of());
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
                return build(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                ApiErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                                ApiErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                                request,
                                List.of());
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
                        HttpServletRequest request) {
                return build(
                                HttpStatus.CONFLICT,
                                ApiErrorCode.CONFLICT.getCode(),
                                ApiErrorCode.CONFLICT.getMessage(),
                                request,
                                List.of());
        }

        @ExceptionHandler({
                        DataAccessException.class,
                        JpaSystemException.class,
                        TransactionSystemException.class,
                        PersistenceException.class
        })
        public ResponseEntity<ApiError> handleDatabase(DataAccessException ex, HttpServletRequest request) {
                log.error("[TraceId: {}] Lỗi Database tại {}: {}",
                                RequestContext.getTraceId(), request.getRequestURI(), ex.getMessage(), ex);
                return build(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                ApiErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                                ApiErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                                request,
                                List.of());
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
                HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
                String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
                return build(
                                status,
                                ex.getStatusCode().toString(),
                                sanitizeMessage(message, ApiErrorCode.INTERNAL_SERVER_ERROR.getMessage()),
                                request,
                                List.of());
        }

        @ExceptionHandler({ NoHandlerFoundException.class, NoResourceFoundException.class })
        public ResponseEntity<ApiError> handleNotFound(HttpServletRequest request) {
                return build(
                                HttpStatus.NOT_FOUND,
                                ApiErrorCode.NOT_FOUND.getCode(),
                                ApiErrorCode.NOT_FOUND.getMessage(),
                                request,
                                List.of());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleUncaught(Exception ex, HttpServletRequest request) {
                log.error("[TraceId: {}] Lỗi không xác định [{}] tại {}: {}",
                                RequestContext.getTraceId(), ex.getClass().getName(), request.getRequestURI(),
                                ex.getMessage(), ex);
                return build(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                ApiErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                                ApiErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                                request,
                                List.of());
        }

        private ResponseEntity<ApiError> build(
                        HttpStatus status,
                        String code,
                        String message,
                        HttpServletRequest request,
                        List<ApiErrorDetail> errors) {

                ApiError body = ApiError.of(
                                RequestContext.getTraceId(),
                                status.value(),
                                code,
                                message,
                                serviceName,
                                request.getRequestURI(),
                                errors);

                return ResponseEntity.status(status).body(body);
        }

        private List<ApiErrorDetail> sanitizeErrors(List<ApiErrorDetail> errors) {
                if (errors == null || errors.isEmpty()) {
                        return List.of();
                }
                return errors.stream().map(error -> new ApiErrorDetail(
                                error.field(),
                                error.code(),
                                sanitizeMessage(error.message(), "Unknown error"))).toList();
        }

        private String sanitizeMessage(String message, String fallback) {
                return ErrorMessageSanitizer.sanitize(message, fallback);
        }
}
