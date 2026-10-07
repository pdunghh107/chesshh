package com.pdunghh.shared.api;

import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public final class ErrorMessageSanitizer {

    private ErrorMessageSanitizer() {
    }

    private static final int MAX_CLIENT_MESSAGE_LENGTH = 240;

    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(?i)(sqlstate|jdbc:|psqlexception|postgresql|preparedstatement|" +
                    "violates unique constraint|could not execute statement|" +
                    "org\\.springframework\\.|org\\.hibernate\\.|jakarta\\.persistence\\.|java\\.sql\\.)");

    public static String sanitize(String message, String fallback) {
        if (!StringUtils.hasText(message)) {
            return fallback;
        }

        String trimmed = message.trim();
        if (trimmed.length() > MAX_CLIENT_MESSAGE_LENGTH) {
            return fallback;
        }

        if (SENSITIVE_PATTERN.matcher(trimmed).find()) {
            return fallback;
        }

        return trimmed;
    }
}
