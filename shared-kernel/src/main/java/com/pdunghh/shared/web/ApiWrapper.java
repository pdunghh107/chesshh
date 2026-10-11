package com.pdunghh.shared.web;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdunghh.shared.api.ApiError;
import com.pdunghh.shared.api.ApiResponse;
import com.pdunghh.shared.api.ApiSuccessCode;
import com.pdunghh.shared.api.PageResponse;
import com.pdunghh.shared.security.RequestContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice(basePackages = "com.pdunghh")
@RequiredArgsConstructor
@Slf4j
public class ApiWrapper implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(
            @NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType)
                || StringHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(
            @Nullable Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response) {

        // 1. Skip nếu body null
        if (body == null) {
            return body;
        }

        // Bỏ qua check ContentType vì String trả về sẽ có selectedContentType =
        // text/plain
        // Nhưng nếu trả về File (octet-stream, pdf, etc...) thì phải skip
        if (selectedContentType != null
                && !MediaType.APPLICATION_JSON.isCompatibleWith(selectedContentType)
                && !MediaType.TEXT_PLAIN.isCompatibleWith(selectedContentType)) {
            return body;
        }

        // 2. Skip nếu đã được bọc sẵn (do Controller tự bọc hoặc ExceptionHandler trả
        // về)
        if (body instanceof ApiResponse<?> || body instanceof ApiError) {
            return body;
        }

        String traceId = RequestContext.getTraceId();

        // 3. Xử lý phân trang
        if (body instanceof Page<?> page) {
            return ApiResponse.paged(traceId, ApiSuccessCode.OK, PageResponse.of(page));
        }
        if (body instanceof PageResponse<?> pageResponse) {
            return ApiResponse.paged(traceId, ApiSuccessCode.OK, pageResponse);
        }

        // 4. Xác định status
        int status = response instanceof ServletServerHttpResponse servletResponse
                ? servletResponse.getServletResponse().getStatus()
                : 200;

        // Bọc data
        ApiResponse<?> apiResponse = status == 201
                ? ApiResponse.created(traceId, ApiSuccessCode.CREATED, body)
                : ApiResponse.ok(traceId, ApiSuccessCode.OK, body);

        // 5. XỬ LÝ ĐẶC BIỆT CHO STRING (Tránh lỗi ClassCastException)
        if (body instanceof String) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            try {
                return objectMapper.writeValueAsString(apiResponse);
            } catch (JsonProcessingException e) {
                log.error("[API WRAPPER]: Lỗi khi parse chuỗi json response", e);
                return body;
            }
        }

        // 6. Trả về Object bình thường cho Jackson tự xử
        return apiResponse;
    }

}
