package com.pdunghh.shared.security;

import java.util.UUID;

public final class RequestContext {
    private static final ThreadLocal<UUID> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();

    private RequestContext() {
    }

    public static UUID getUserId() {
        return USER_ID.get();
    }

    public static void setUserID(UUID userId) {
        USER_ID.set(userId);
    }

    public static String getCorrelationId() {
        return CORRELATION_ID.get();
    }

    public static void setCorrelationId(String correlationId) {
        CORRELATION_ID.set(correlationId);
    }

    public static void clear() {
        USER_ID.remove();
        CORRELATION_ID.remove();
    }
}
