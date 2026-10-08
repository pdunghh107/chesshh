package com.pdunghh.auth.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateMeRequest(
                @Size(max = 500, message = "Đường dẫn ảnh đại diện không được vượt quá 500 ký tự") String avatarUrl) {
}
