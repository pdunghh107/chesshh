package com.pdunghh.shared.web;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.pdunghh.shared.api.ApiError;
import com.pdunghh.shared.api.ApiResponse;
import com.pdunghh.shared.api.PageResponse;

@RestControllerAdvice
public class ApiEnvelopeResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(
            @Nullable Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response) {

        // 1. Check content type
        if (selectedContentType == null
                || !MediaType.APPLICATION_JSON.isCompatibleWith(selectedContentType)) {
            return body;
        }

        // 2. Get status
        int status = response instanceof ServletServerHttpResponse servletResponse
                ? servletResponse.getServletResponse().getStatus()
                : 200;

        // 3. Case status >= 400

        if (status >= 400) {
            return body;
        }

        // 4. Wrap api response or api error
        if (body instanceof ApiResponse<?> || body instanceof ApiError) {
            return body;
        }

        // 5. Case status == 204
        if (status == 204) {
            return null;
        }

        // 6. Wrap page respose
        if (body instanceof Page<?> page) {
            return ApiResponse.paged(PageResponse.from(page));
        }

        if (body instanceof PageResponse<?> pageResponse) {
            return ApiResponse.paged(pageResponse);
        }

        // 7. Case body = null
        if (body == null) {
            return status == 201 ? ApiResponse.created() : ApiResponse.ok();
        }

        // 8. Case status == 201
        if (status == 201) {
            return ApiResponse.created(body);
        }

        // TODO: Co xu ly case message va case content type trong truong hop download
        // khong ???

        return ApiResponse.ok(body);
    }

}
