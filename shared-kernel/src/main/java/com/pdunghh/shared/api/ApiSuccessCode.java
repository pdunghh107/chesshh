package com.pdunghh.shared.api;

public enum ApiSuccessCode {
    OK("OK", "Thao tác thành công"),
    CREATED("CREATED", "Tạo mới thành công"),
    ACCEPTED("ACCEPTED", "Đã chấp nhận"),
    NO_CONTENT("NO_CONTENT", "Không có nội dung"),
    RESET_CONTENT("RESET_CONTENT", "Đặt lại nội dung"),
    PARTIAL_CONTENT("PARTIAL_CONTENT", "Nội dung một phần"),
    MOVED_PERMANENTLY("MOVED_PERMANENTLY", "Di chuyển vĩnh viễn"),
    FOUND("FOUND", "Tìm thấy"),
    SEE_OTHER("SEE_OTHER", "Xem khác"),
    NOT_MODIFIED("NOT_MODIFIED", "Không thay đổi"),
    TEMPORARY_REDIRECT("TEMPORARY_REDIRECT", "Chuyển hướng tạm thời"),
    PERMANENT_REDIRECT("PERMANENT_REDIRECT", "Chuyển hướng vĩnh viễn");

    private final String code;
    private final String message;

    ApiSuccessCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
