package com.pdunghh.shared.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pdunghh.shared.api.ApiError;
import com.pdunghh.shared.api.ApiErrorCode;
import com.pdunghh.shared.api.ApiResponse;
import com.pdunghh.shared.api.PageResponse;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
public class ApiEnvelopeResponseBodyAdviceTest {

    private ApiEnvelopeResponseBodyAdvice advice;

    @Mock
    private MethodParameter returnType;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @BeforeEach
    void setUp() {
        advice = new ApiEnvelopeResponseBodyAdvice();
    }

    @Test
    void supports_ShouldReturnTrue_ForJacksonAndStringConverters() {
        assertTrue(advice.supports(Objects.requireNonNull(returnType), MappingJackson2HttpMessageConverter.class));
        assertTrue(advice.supports(Objects.requireNonNull(returnType), StringHttpMessageConverter.class));
    }

    @Test
    void supports_ShouldReturnFalse_ForOtherConverters() {
        assertFalse(advice.supports(Objects.requireNonNull(returnType), ByteArrayHttpMessageConverter.class));
    }

    @Test
    void beforeBodyWrite_ShouldReturnBody_WhenMediaTypeIsNotJson() {
        Object body = "Test Body";
        Object result = advice.beforeBodyWrite(
                body,
                Objects.requireNonNull(returnType),
                Objects.requireNonNull(MediaType.TEXT_PLAIN),
                MappingJackson2HttpMessageConverter.class,
                Objects.requireNonNull(request),
                Objects.requireNonNull(response));

        assertEquals(body, result);
    }

    @Test
    void beforeBodyWrite_ShouldReturnBody_WhenStatusIsError400OrAbove() {
        MockHttpServletResponse mockServletResponse = new MockHttpServletResponse();
        mockServletResponse.setStatus(400);
        ServletServerHttpResponse servletServerHttpResponse = new ServletServerHttpResponse(mockServletResponse);

        Object body = "Error Body";
        Object result = advice.beforeBodyWrite(
                body,
                Objects.requireNonNull(returnType),
                Objects.requireNonNull(MediaType.APPLICATION_JSON),
                MappingJackson2HttpMessageConverter.class,
                Objects.requireNonNull(request),
                servletServerHttpResponse);

        assertEquals(body, result);
    }

    @Test
    void beforeBodyWrite_ShouldReturnNull_WhenStatusIs204NoContent() {
        MockHttpServletResponse mockServletResponse = new MockHttpServletResponse();
        mockServletResponse.setStatus(204);
        ServletServerHttpResponse servletServerHttpResponse = new ServletServerHttpResponse(mockServletResponse);

        Object result = advice.beforeBodyWrite(
                "Body",
                Objects.requireNonNull(returnType),
                Objects.requireNonNull(MediaType.APPLICATION_JSON),
                MappingJackson2HttpMessageConverter.class,
                Objects.requireNonNull(request),
                servletServerHttpResponse);

        assertNull(result);
    }

    @Test
    void beforeBodyWrite_ShouldNotWrap_WhenBodyIsAlreadyApiResponse() {
        ApiResponse<String> body = ApiResponse.ok("Data");
        Object result = advice.beforeBodyWrite(
                body,
                Objects.requireNonNull(returnType),
                Objects.requireNonNull(MediaType.APPLICATION_JSON),
                MappingJackson2HttpMessageConverter.class,
                Objects.requireNonNull(request),
                Objects.requireNonNull(response));

        assertEquals(body, result);
        assertInstanceOf(ApiResponse.class, result);
    }

    @Test
    void beforeBodyWrite_ShouldNotWrap_WhenBodyIsAlreadyApiError() {
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        ApiError body = mockError(HttpStatus.BAD_REQUEST, servletRequest);
        Object result = advice.beforeBodyWrite(
                body,
                Objects.requireNonNull(returnType),
                Objects.requireNonNull(MediaType.APPLICATION_JSON),
                MappingJackson2HttpMessageConverter.class,
                Objects.requireNonNull(request),
                Objects.requireNonNull(response));

        assertEquals(body, result);
        assertInstanceOf(ApiError.class, result);
    }

    @Test
    void beforeBodyWrite_ShouldWrapInCreated_WhenBodyIsNullAndStatusIs201() {
        MockHttpServletResponse mockServletResponse = new MockHttpServletResponse();
        mockServletResponse.setStatus(201);
        ServletServerHttpResponse servletServerHttpResponse = new ServletServerHttpResponse(mockServletResponse);

        Object result = advice.beforeBodyWrite(
                null,
                Objects.requireNonNull(returnType),
                Objects.requireNonNull(MediaType.APPLICATION_JSON),
                MappingJackson2HttpMessageConverter.class,
                Objects.requireNonNull(request),
                servletServerHttpResponse);

        assertInstanceOf(ApiResponse.class, result);
        ApiResponse<?> apiResponse = (ApiResponse<?>) result;
        assertTrue(apiResponse.success());
        assertNull(apiResponse.data());
    }

    @Test
    void beforeBodyWrite_ShouldWrapInOk_WhenBodyIsNullAndStatusIs200() {
        MockHttpServletResponse mockServletResponse = new MockHttpServletResponse();
        mockServletResponse.setStatus(200);
        ServletServerHttpResponse servletServerHttpResponse = new ServletServerHttpResponse(mockServletResponse);

        Object result = advice.beforeBodyWrite(
                null,
                Objects.requireNonNull(returnType),
                Objects.requireNonNull(MediaType.APPLICATION_JSON),
                MappingJackson2HttpMessageConverter.class,
                Objects.requireNonNull(request),
                servletServerHttpResponse);

        assertInstanceOf(ApiResponse.class, result);
        ApiResponse<?> apiResponse = (ApiResponse<?>) result;
        assertTrue(apiResponse.success());
        assertNull(apiResponse.data());
    }

    @Test
    void beforeBodyWrite_ShouldWrapInCreated_WhenStatusIs201AndHasBody() {
        MockHttpServletResponse mockServletResponse = new MockHttpServletResponse();
        mockServletResponse.setStatus(201);
        ServletServerHttpResponse servletServerHttpResponse = new ServletServerHttpResponse(mockServletResponse);

        Object body = new Object();
        Object result = advice.beforeBodyWrite(
                body,
                Objects.requireNonNull(returnType),
                Objects.requireNonNull(MediaType.APPLICATION_JSON),
                MappingJackson2HttpMessageConverter.class,
                Objects.requireNonNull(request),
                servletServerHttpResponse);

        assertInstanceOf(ApiResponse.class, result);
        ApiResponse<?> apiResponse = (ApiResponse<?>) result;
        assertTrue(apiResponse.success());
        assertEquals(body, apiResponse.data());
    }

    @Test
    void beforeBodyWrite_ShouldWrapInOk_WhenStatusIs200AndHasBody() {
        MockHttpServletResponse mockServletResponse = new MockHttpServletResponse();
        mockServletResponse.setStatus(200);
        ServletServerHttpResponse servletServerHttpResponse = new ServletServerHttpResponse(mockServletResponse);

        Object body = new Object();
        Object result = advice.beforeBodyWrite(
                body,
                Objects.requireNonNull(returnType),
                Objects.requireNonNull(MediaType.APPLICATION_JSON),
                MappingJackson2HttpMessageConverter.class,
                Objects.requireNonNull(request),
                servletServerHttpResponse);

        assertInstanceOf(ApiResponse.class, result);
        ApiResponse<?> apiResponse = (ApiResponse<?>) result;
        assertTrue(apiResponse.success());
        assertEquals(body, apiResponse.data());
    }

    @Test
    void beforeBodyWrite_ShouldWrapPage_WhenBodyIsPage() {
        Page<String> page = new PageImpl<>(Objects.requireNonNull(List.of("Item1", "Item2")));

        Object result = advice.beforeBodyWrite(
                page,
                Objects.requireNonNull(returnType),
                Objects.requireNonNull(MediaType.APPLICATION_JSON),
                MappingJackson2HttpMessageConverter.class,
                Objects.requireNonNull(request),
                Objects.requireNonNull(response));

        System.out.println(result);

        assertInstanceOf(ApiResponse.class, result);
        ApiResponse<?> apiResponse = (ApiResponse<?>) result;
        assertInstanceOf(PageResponse.class, apiResponse.data());
    }

    @Test
    void beforeBodyWrite_ShouldWrapPageResponse_WhenBodyIsPageResponse() {
        Page<String> page = new PageImpl<>(Objects.requireNonNull(List.of("Item1", "Item2")));
        PageResponse<String> pageResponse = PageResponse.from(page);

        Object result = advice.beforeBodyWrite(
                pageResponse,
                Objects.requireNonNull(returnType),
                Objects.requireNonNull(MediaType.APPLICATION_JSON),
                MappingJackson2HttpMessageConverter.class,
                Objects.requireNonNull(request),
                Objects.requireNonNull(response));

        assertInstanceOf(ApiResponse.class, result);
        ApiResponse<?> apiResponse = (ApiResponse<?>) result;
        assertEquals(pageResponse, apiResponse.data());
    }

    @Test
    void beforeBodyWrite_ShouldReturnApiResponse_WhenBodyIsStringAndConverterIsJackson() {
        String message = "Success Message";

        Object result = advice.beforeBodyWrite(
                message,
                Objects.requireNonNull(returnType),
                Objects.requireNonNull(MediaType.APPLICATION_JSON),
                MappingJackson2HttpMessageConverter.class,
                Objects.requireNonNull(request),
                Objects.requireNonNull(response));

        System.out.println(result);

        assertInstanceOf(ApiResponse.class, result);
        ApiResponse<?> apiResponse = (ApiResponse<?>) result;
        assertEquals(message, apiResponse.message());
        assertNull(apiResponse.data());
    }

    @Test
    void beforeBodyWrite_ShouldReturnJsonString_WhenBodyIsStringAndConverterIsStringConverter()
            throws JsonProcessingException {
        String body = "Success Message";

        Object result = advice.beforeBodyWrite(
                body,
                Objects.requireNonNull(returnType),
                Objects.requireNonNull(MediaType.APPLICATION_JSON),
                StringHttpMessageConverter.class,
                Objects.requireNonNull(request),
                Objects.requireNonNull(response));

        System.out.println(result);

        assertTrue(result instanceof String);
        String jsonResult = (String) result;
        assertTrue(jsonResult.contains("\"message\":\"Success Message\""));
    }

    ApiError mockError(HttpStatus status, HttpServletRequest request) {
        String code = ApiErrorCode.getDefaultErrorCode(status.value());
        String message = ApiErrorCode.getDefaultErrorMessage(status.value());
        return ApiError.build(status, code, message, request, null, null);
    }

}
