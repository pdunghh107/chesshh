package com.pdunghh.shared.api;

public enum ApiSuccessCode {
    OK("OK"),
    CREATED("CREATED");

    private final String value;

    ApiSuccessCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
