package com.pdunghh.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pdunghh.auth.entity.UserEntity;

public record LoginResponse(
        String accessToken,
        @JsonIgnore String refreshToken,
        UserResponse user) {

    public static LoginResponse from(String accessToken, String refreshToken, UserEntity user) {
        return new LoginResponse(accessToken, refreshToken, UserResponse.from(user));
    }

}
