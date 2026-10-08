package com.pdunghh.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record LoginResponse(
                String accessToken,
                @JsonIgnore String refreshToken,
                UserResponse user) {
}
