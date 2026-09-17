package com.pdunghh.auth.dto.response;

import com.pdunghh.auth.entity.UserEntity;

public record UserResponse(
        String fullName,
        String email,
        String phone,
        boolean isActive) {

    public static UserResponse from(UserEntity user) {
        return new UserResponse(
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.isActive());
    }
}
