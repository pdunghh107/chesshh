package com.pdunghh.shared.api;

import org.springframework.validation.FieldError;

public record ApiErrorDetail(
                String code,
                String field,
                String message) {

        public static ApiErrorDetail build(ApiErrorDetail d, String message) {
                return new ApiErrorDetail(d.code(), d.field(), message);
        }

        public static ApiErrorDetail build(FieldError fieldError, String message) {
                return new ApiErrorDetail(fieldError.getCode(), fieldError.getField(), message);
        }

        public static ApiErrorDetail build(FieldError fieldError) {
                return new ApiErrorDetail(fieldError.getCode(), fieldError.getField(), fieldError.getDefaultMessage());
        }
}