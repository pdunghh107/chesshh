package com.pdunghh.shared.utils;

import java.util.function.Consumer;

import org.springframework.util.StringUtils;

public final class SharedUtils {

    private SharedUtils() {
    }

    public static String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    public static void shouldUpdate(String newValue, String currentValue, Consumer<String> setter, boolean shouldTrim) {
        if (StringUtils.hasText(newValue)) {
            String processValue = shouldTrim ? newValue.trim() : newValue;
            if (!processValue.equals(currentValue)) {
                setter.accept(processValue);
            }
        }
    }
}
