package com.pdunghh.auth.service;

import com.pdunghh.auth.dto.request.LoginRequest;
import com.pdunghh.auth.dto.request.RegisterRequest;
import com.pdunghh.auth.dto.request.UpdateMeRequest;
import com.pdunghh.auth.dto.response.LoginResponse;
import com.pdunghh.auth.dto.response.UserResponse;

public interface AuthService {
    LoginResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse getMe();

    UserResponse updateMe(UpdateMeRequest request);

    String logout(String refreshToken);

    String logoutAll();

    LoginResponse refreshToken(String refreshToken);

    void deactivateAccount();
}
