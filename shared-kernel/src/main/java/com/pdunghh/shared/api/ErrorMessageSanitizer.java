package com.pdunghh.shared.api;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.pdunghh.shared.utils.CommonString;

public final class ErrorMessageSanitizer {

    private ErrorMessageSanitizer() {
    }

    private static final int MAX_CLIENT_MESSAGE_LENGTH = 240;

    private static final String[] SENSITIVE_MARKERS = {
            // SQL DML & DDL
            "select ", "insert ", "update ", "delete ", " from ", " where ", " join ", "alter ", "drop ", "truncate ",
            // Database & Driver
            "sqlstate", "sql \\[", "jdbc", "hibernate", "psqlexception", "postgresql", "preparedstatement",
            // Constraints
            "constraint \\[", "duplicate key value", "violates unique constraint", "could not execute statement",
            "syntax error at or near",
            // Packages
            "org.springframework.dao", "org.hibernate", "jakarta.persistence", "java.sql", "java.lang\\.", "java.io\\.",
            "java.net\\.",
            // Network & Infrastructure
            "connection refused", "connection reset", "timed out", "broken pipe", "redis", "lettuce", "amqp",
            // Security
            "password", "secret", "token"
    };

    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(String.join("|", SENSITIVE_MARKERS));

    public static String sanitizeMessage(String message, String fallback) {
        if (!StringUtils.hasText(message)) {
            return fallback;
        }
        String trimmed = message.trim();
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        if (trimmed.length() > MAX_CLIENT_MESSAGE_LENGTH || SENSITIVE_PATTERN.matcher(normalized).find()) {
            return fallback;
        }
        return trimmed;
    }

    private static final String[] INFRA_MARKERS = {
            // Database & JPA
            "sql", "jdbc", "hibernate", "persistence", "dataaccess",
            "datasource", "postgresql", "dao",
            // Spring & Framework
            "springframework", "transaction",
            // Network & I/O
            "net", "io", "socket",
            // Serialization & JSON
            "jackson", "fasterxml",
            // Caching & Messaging
            "redis", "lettuce", "amqp", "kafka",
            // Web Server
            "tomcat", "catalina", "netty"
    };

    private static final Pattern INFRA_PATTERN = Pattern.compile(String.join("|", INFRA_MARKERS),
            Pattern.CASE_INSENSITIVE);

    public static boolean isInfrastructureError(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        String classname = throwable.getClass().getName();
        return INFRA_PATTERN.matcher(classname).find();
    }

    public static List<Object> sanitizeDetails(List<?> details) {
        return details == null ? List.of() : details.stream().map(detail -> {
            if (detail instanceof String s) {
                return getDefaultDetailsMessage(s);
            }
            if (detail instanceof ApiErrorDetail d) {
                return ApiErrorDetail.build(d, getDefaultDetailsMessage(d.message()));
            }
            return detail;
        }).toList();
    }

    private static String getDefaultDetailsMessage(String message) {
        return StringUtils.hasText(message)
                ? ErrorMessageSanitizer.sanitizeMessage(message, CommonString.BAD_FIELD_VALIDATION)
                : CommonString.BAD_FIELD_VALIDATION;
    }
}
