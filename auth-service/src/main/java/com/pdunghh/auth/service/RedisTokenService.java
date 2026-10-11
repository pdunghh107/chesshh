package com.pdunghh.auth.service;

import com.pdunghh.auth.dto.response.LoginResponse;
import com.pdunghh.auth.entity.User;

public interface RedisTokenService {

    LoginResponse generateLoginResponse(User user);

    void revokeAllTokens(User user);

    void revokeToken(String refreshToken);

    LoginResponse refreshTokens(String refreshToken);
}
