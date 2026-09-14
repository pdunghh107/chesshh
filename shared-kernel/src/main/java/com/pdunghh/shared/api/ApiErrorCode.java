package com.pdunghh.shared.api;

import com.pdunghh.shared.utils.CommonString;

public enum ApiErrorCode {

    DEFAULT_ERROR("DEFAULT_ERROR"),

    BAD_REQUEST("BAD_REQUEST"),
    UNAUTHORIZED("UNAUTHORIZED"),
    PAYMENT_REQUIRED("PAYMENT_REQUIRED"),
    FORBIDDEN("FORBIDDEN"),
    NOT_FOUND("NOT_FOUND"),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED"),
    NOT_ACCEPTABLE("NOT_ACCEPTABLE"),
    PROXY_AUTHENTICATION_REQUIRED("PROXY_AUTHENTICATION_REQUIRED"),
    REQUEST_TIMEOUT("REQUEST_TIMEOUT"),
    CONFLICT("CONFLICT"),
    GONE("GONE"),
    LENGTH_REQUIRED("LENGTH_REQUIRED"),
    PRECONDITION_FAILED("PRECONDITION_FAILED"),
    PAYLOAD_TOO_LARGE("PAYLOAD_TOO_LARGE"),
    URI_TOO_LONG("URI_TOO_LONG"),
    UNSUPPORTED_MEDIA_TYPE("UNSUPPORTED_MEDIA_TYPE"),
    RANGE_NOT_SATISFIABLE("RANGE_NOT_SATISFIABLE"),
    EXPECTATION_FAILED("EXPECTATION_FAILED"),
    I_AM_A_TEAPOT("I_AM_A_TEAPOT"),
    MISDIRECTED_REQUEST("MISDIRECTED_REQUEST"),
    UNPROCESSABLE_ENTITY("UNPROCESSABLE_ENTITY"),
    LOCKED("LOCKED"),
    FAILED_DEPENDENCY("FAILED_DEPENDENCY"),
    TOO_EARLY("TOO_EARLY"),
    UPGRADE_REQUIRED("UPGRADE_REQUIRED"),
    PRECONDITION_REQUIRED("PRECONDITION_REQUIRED"),
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS"),
    REQUEST_HEADER_FIELDS_TOO_LARGE("REQUEST_HEADER_FIELDS_TOO_LARGE"),
    UNAVAILABLE_FOR_LEGAL_REASONS("UNAVAILABLE_FOR_LEGAL_REASONS"),

    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR"),
    NOT_IMPLEMENTED("NOT_IMPLEMENTED"),
    BAD_GATEWAY("BAD_GATEWAY"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE"),
    GATEWAY_TIMEOUT("GATEWAY_TIMEOUT"),
    HTTP_VERSION_NOT_SUPPORTED("HTTP_VERSION_NOT_SUPPORTED"),
    VARIANT_ALSO_NEGOTIATES("VARIANT_ALSO_NEGOTIATES"),
    INSUFFICIENT_STORAGE("INSUFFICIENT_STORAGE"),
    LOOP_DETECTED("LOOP_DETECTED"),
    NOT_EXTENDED("NOT_EXTENDED"),
    NETWORK_AUTHENTICATION_REQUIRED("NETWORK_AUTHENTICATION_REQUIRED");

    private final String value;

    ApiErrorCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static String getDefaultErrorCode(int status) {
        return switch (status) {
            case 400 -> BAD_REQUEST.getValue();
            case 401 -> UNAUTHORIZED.getValue();
            case 402 -> PAYMENT_REQUIRED.getValue();
            case 403 -> FORBIDDEN.getValue();
            case 404 -> NOT_FOUND.getValue();
            case 405 -> METHOD_NOT_ALLOWED.getValue();
            case 406 -> NOT_ACCEPTABLE.getValue();
            case 407 -> PROXY_AUTHENTICATION_REQUIRED.getValue();
            case 408 -> REQUEST_TIMEOUT.getValue();
            case 409 -> CONFLICT.getValue();
            case 410 -> GONE.getValue();
            case 411 -> LENGTH_REQUIRED.getValue();
            case 412 -> PRECONDITION_FAILED.getValue();
            case 413 -> PAYLOAD_TOO_LARGE.getValue();
            case 414 -> URI_TOO_LONG.getValue();
            case 415 -> UNSUPPORTED_MEDIA_TYPE.getValue();
            case 416 -> RANGE_NOT_SATISFIABLE.getValue();
            case 417 -> EXPECTATION_FAILED.getValue();
            case 418 -> I_AM_A_TEAPOT.getValue();
            case 421 -> MISDIRECTED_REQUEST.getValue();
            case 422 -> UNPROCESSABLE_ENTITY.getValue();
            case 423 -> LOCKED.getValue();
            case 424 -> FAILED_DEPENDENCY.getValue();
            case 425 -> TOO_EARLY.getValue();
            case 426 -> UPGRADE_REQUIRED.getValue();
            case 428 -> PRECONDITION_REQUIRED.getValue();
            case 429 -> TOO_MANY_REQUESTS.getValue();
            case 431 -> REQUEST_HEADER_FIELDS_TOO_LARGE.getValue();
            case 451 -> UNAVAILABLE_FOR_LEGAL_REASONS.getValue();
            case 500 -> INTERNAL_SERVER_ERROR.getValue();
            case 501 -> NOT_IMPLEMENTED.getValue();
            case 502 -> BAD_GATEWAY.getValue();
            case 503 -> SERVICE_UNAVAILABLE.getValue();
            case 504 -> GATEWAY_TIMEOUT.getValue();
            case 505 -> HTTP_VERSION_NOT_SUPPORTED.getValue();
            case 506 -> VARIANT_ALSO_NEGOTIATES.getValue();
            case 507 -> INSUFFICIENT_STORAGE.getValue();
            case 508 -> LOOP_DETECTED.getValue();
            case 510 -> NOT_EXTENDED.getValue();
            case 511 -> NETWORK_AUTHENTICATION_REQUIRED.getValue();
            default -> DEFAULT_ERROR.getValue();
        };
    }

    public static String getDefaultErrorMessage(int status) {
        return switch (status) {
            case 400 -> CommonString.BAD_REQUEST_MESSAGE;
            case 401 -> CommonString.UNAUTHORIZED_MESSAGE;
            case 402 -> CommonString.PAYMENT_REQUIRED_MESSAGE;
            case 403 -> CommonString.FORBIDDEN_MESSAGE;
            case 404 -> CommonString.NOT_FOUND_MESSAGE;
            case 405 -> CommonString.METHOD_NOT_ALLOWED_MESSAGE;
            case 406 -> CommonString.NOT_ACCEPTABLE_MESSAGE;
            case 407 -> CommonString.PROXY_AUTHENTICATION_REQUIRED_MESSAGE;
            case 408 -> CommonString.REQUEST_TIMEOUT_MESSAGE;
            case 409 -> CommonString.CONFLICT_MESSAGE;
            case 410 -> CommonString.GONE_MESSAGE;
            case 411 -> CommonString.LENGTH_REQUIRED_MESSAGE;
            case 412 -> CommonString.PRECONDITION_FAILED_MESSAGE;
            case 413 -> CommonString.PAYLOAD_TOO_LARGE_MESSAGE;
            case 414 -> CommonString.URI_TOO_LONG_MESSAGE;
            case 415 -> CommonString.UNSUPPORTED_MEDIA_TYPE_MESSAGE;
            case 416 -> CommonString.RANGE_NOT_SATISFIABLE_MESSAGE;
            case 417 -> CommonString.EXPECTATION_FAILED_MESSAGE;
            case 418 -> CommonString.I_AM_A_TEAPOT_MESSAGE;
            case 421 -> CommonString.MISDIRECTED_REQUEST_MESSAGE;
            case 422 -> CommonString.UNPROCESSABLE_ENTITY_MESSAGE;
            case 423 -> CommonString.LOCKED_MESSAGE;
            case 424 -> CommonString.FAILED_DEPENDENCY_MESSAGE;
            case 425 -> CommonString.TOO_EARLY_MESSAGE;
            case 426 -> CommonString.UPGRADE_REQUIRED_MESSAGE;
            case 428 -> CommonString.PRECONDITION_REQUIRED_MESSAGE;
            case 429 -> CommonString.TOO_MANY_REQUESTS_MESSAGE;
            case 431 -> CommonString.REQUEST_HEADER_FIELDS_TOO_LARGE_MESSAGE;
            case 451 -> CommonString.UNAVAILABLE_FOR_LEGAL_REASONS_MESSAGE;
            case 500 -> CommonString.INTERNAL_SERVER_ERROR_MESSAGE;
            case 501 -> CommonString.NOT_IMPLEMENTED_MESSAGE;
            case 502 -> CommonString.BAD_GATEWAY_MESSAGE;
            case 503 -> CommonString.SERVICE_UNAVAILABLE_MESSAGE;
            case 504 -> CommonString.GATEWAY_TIMEOUT_MESSAGE;
            case 505 -> CommonString.HTTP_VERSION_NOT_SUPPORTED_MESSAGE;
            case 506 -> CommonString.VARIANT_ALSO_NEGOTIATES_MESSAGE;
            case 507 -> CommonString.INSUFFICIENT_STORAGE_MESSAGE;
            case 508 -> CommonString.LOOP_DETECTED_MESSAGE;
            case 510 -> CommonString.NOT_EXTENDED_MESSAGE;
            case 511 -> CommonString.NETWORK_AUTHENTICATION_REQUIRED_MESSAGE;
            default -> CommonString.DEFAULT_ERROR_MESSAGE;
        };
    }

}