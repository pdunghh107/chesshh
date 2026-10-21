package com.pdunghh.auth.service;

import com.pdunghh.auth.dto.response.LoginResponse;
import com.pdunghh.auth.entity.User;

public interface TokenService {
    LoginResponse buildLoginResponse(User user);

    void revokeAllRefreshTokensByUser(User user);

    void revokeRefreshTokenIfPresent(String refreshToken);

    LoginResponse refreshTokens(String refreshToken);
}
