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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdunghh.shared.api.ApiError;
import com.pdunghh.shared.api.ApiResponse;
import com.pdunghh.shared.api.PageResponse;

import lombok.extern.slf4j.Slf4j;

// restcontrolleradvice luon chay khi controller return
@RestControllerAdvice(basePackages = "com.pdunghh")
@Slf4j
public class ApiEnvelopeResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

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

        if (selectedContentType == null
                || !MediaType.APPLICATION_JSON.isCompatibleWith(selectedContentType)) {
            return body;
        }

        int status = response instanceof ServletServerHttpResponse servletResponse
                ? servletResponse.getServletResponse().getStatus()
                : 200;

        if (body == null) {
            return buildApiResponseMethod(status, null);
        }

        if (status >= 400) {
            return body;
        }

        if (body instanceof ApiResponse<?> || body instanceof ApiError) {
            return body;
        }

        if (status == 204) {
            return null;
        }

        ApiResponse<?> pageResponse = resolvePageResponse(body);
        if (pageResponse != null) {
            return pageResponse;
        }

        Object stringResponse = resolveStringResponse(status, body, selectedConverterType);
        if (stringResponse != null) {
            return stringResponse;
        }

        return buildApiResponseMethod(status, body);
    }

    // case page
    private ApiResponse<?> resolvePageResponse(Object body) {
        if (body instanceof Page<?> page) {
            return ApiResponse.paged(PageResponse.from(page));
        }
        if (body instanceof PageResponse<?> pageResponse) {
            return ApiResponse.paged(pageResponse);
        }
        return null;
    }

    // case string
    private Object resolveStringResponse(int status, Object body, Class<?> converterType) {
        if (body instanceof String message) {
            ApiResponse<?> response = buildApiResponseMethod(status, message);

            return resolveJsonWrappedToString(response, converterType);
        }

        return null;
    }

    // case json wrapped to string
    private Object resolveJsonWrappedToString(ApiResponse<?> response, Class<?> converterType) {
        if (StringHttpMessageConverter.class.isAssignableFrom(converterType)) {
            try {
                return objectMapper.writeValueAsString(response);
            } catch (Exception e) {
                log.error("[API WRAPPER CASE STRING]: CONVERT STRING SANG API RESPONSE THAT BAI", e);
                return response;
            }
        }

        return response;
    }

    // resolve created of ok
    private ApiResponse<?> buildApiResponseMethod(int status, Object body) {
        return status == 201 ? ApiResponse.created(body) : ApiResponse.ok(body);
    }

    private ApiResponse<?> buildApiResponseMethod(int status, String message) {
        return status == 201 ? ApiResponse.created(message) : ApiResponse.ok(message);
    }

}
