package com.pdunghh.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.pdunghh.shared.annotation.ValidPassword;

public record RegisterRequest(
                @NotBlank(message = "Tên đăng nhập không được để trống") @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự") String username,

                @NotBlank(message = "Email không được để trống") @Email(message = "Email không đúng định dạng") String email,

                @ValidPassword String password) {
}
