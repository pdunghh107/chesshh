package com.pdunghh.shared.api;

public record ApiErrorDetail(
        String field,
        String code,
        String message) {

    public static ApiErrorDetail of(
            String field,
            String code,
            String message) {
        return new ApiErrorDetail(field, code, message);
    }
}
