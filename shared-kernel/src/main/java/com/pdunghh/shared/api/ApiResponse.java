package com.pdunghh.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pdunghh.shared.security.RequestContext;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String code,
        T data,
        String message,
        String correlationId) {

    public static <T> ApiResponse<T> ok(ApiSuccessCode code, T data, String message) {
        return new ApiResponse<>(true, code.getValue(), data, message, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<T>(true, ApiSuccessCode.OK.getValue(), null, null, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<T> ok(ApiSuccessCode code) {
        return new ApiResponse<T>(true, code.getValue(), null, null, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<T>(true, ApiSuccessCode.OK.getValue(), data, null, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<T>(true, ApiSuccessCode.OK.getValue(), null, message, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<T> ok(ApiSuccessCode code, T data) {
        return new ApiResponse<T>(true, code.getValue(), data, null, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<T> ok(ApiSuccessCode code, String message) {
        return new ApiResponse<T>(true, code.getValue(), null, message, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<T>(true, ApiSuccessCode.OK.getValue(), data, message, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<T> created(ApiSuccessCode code, T data, String message) {
        return new ApiResponse<>(true, code.getValue(), data, message, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<T> created() {
        return new ApiResponse<T>(true, ApiSuccessCode.CREATED.getValue(), null, null,
                RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<T> created(ApiSuccessCode code) {
        return new ApiResponse<T>(true, code.getValue(), null, null, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<T>(true, ApiSuccessCode.CREATED.getValue(), data, null,
                RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<T> created(String message) {
        return new ApiResponse<T>(true, ApiSuccessCode.CREATED.getValue(), null, message,
                RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<T> created(ApiSuccessCode code, T data) {
        return new ApiResponse<T>(true, code.getValue(), data, null, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<T> created(ApiSuccessCode code, String message) {
        return new ApiResponse<T>(true, code.getValue(), null, message, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<T>(true, ApiSuccessCode.CREATED.getValue(), data, message,
                RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<PageResponse<T>> paged(ApiSuccessCode code, PageResponse<T> page, String message) {
        return new ApiResponse<>(true, code.getValue(), page, message, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<PageResponse<T>> paged() {
        return new ApiResponse<>(true, ApiSuccessCode.OK.getValue(), null, null, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<PageResponse<T>> paged(ApiSuccessCode code) {
        return new ApiResponse<>(true, code.getValue(), null, null, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<PageResponse<T>> paged(PageResponse<T> page) {
        return new ApiResponse<>(true, ApiSuccessCode.OK.getValue(), page, null, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<PageResponse<T>> paged(String message) {
        return new ApiResponse<>(true, ApiSuccessCode.OK.getValue(), null, message, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<PageResponse<T>> paged(ApiSuccessCode code, PageResponse<T> page) {
        return new ApiResponse<>(true, code.getValue(), page, null, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<PageResponse<T>> paged(ApiSuccessCode code, String message) {
        return new ApiResponse<>(true, code.getValue(), null, message, RequestContext.getCorrelationId());
    }

    public static <T> ApiResponse<PageResponse<T>> paged(PageResponse<T> page, String message) {
        return new ApiResponse<>(true, ApiSuccessCode.OK.getValue(), page, message, RequestContext.getCorrelationId());
    }

}
