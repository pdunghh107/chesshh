package com.pdunghh.shared.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.pdunghh.shared.security.RequestContext;

import jakarta.servlet.http.HttpServletRequest;

public record ApiError(
                Instant timestamp,
                int status,
                String error,
                String code,
                String message,
                String path,
                String traceId,
                String service,
                List<Object> details) {

        public static ApiError build(
                        HttpStatus status,
                        String code,
                        String message,
                        HttpServletRequest request,
                        String serviceName,
                        List<?> details) {

                return new ApiError(
                                Instant.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                code,
                                message,
                                request.getRequestURI(),
                                RequestContext.getCorrelationId(),
                                serviceName,
                                details == null ? List.of() : new ArrayList<>(details));

        }

}
