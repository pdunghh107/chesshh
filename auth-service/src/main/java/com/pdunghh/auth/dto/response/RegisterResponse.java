package com.pdunghh.auth.dto.response;

import com.pdunghh.auth.entity.UserEntity;

public record RegisterResponse(
        String accessToken,
        UserResponse user) {

    public static RegisterResponse from(String accessToken, UserEntity user) {
        return new RegisterResponse(accessToken, UserResponse.from(user));
    }
}
