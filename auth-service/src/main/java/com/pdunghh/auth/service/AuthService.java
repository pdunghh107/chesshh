package com.pdunghh.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pdunghh.auth.dto.request.RegisterRequest;
import com.pdunghh.auth.dto.response.RegisterResponse;
import com.pdunghh.auth.entity.UserEntity;
import com.pdunghh.auth.exception.AuthException;
import com.pdunghh.auth.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // validate string
    public static final String FULL_NAME_REQUIRED = "Họ tên không được để trống";
    public static final String FULL_NAME_INVALID = "Họ tên không hợp lệ";
    public static final String EMAIL_REQUIRED = "Email không được để trống";
    public static final String EMAIL_INVALID = "Email không hợp lệ";
    public static final String PHONE_INVALID = "Số điện thoại không hợp lệ";
    public static final String PASSWORD_REQUIRED = "Mật khẩu không được để trống";
    public static final String PASSWORD_INVALID = "Mật khẩu không hợp lệ";

    public static final String ROLE_USER = "user";
    // register

    @Transactional
    public RegisterResponse register(RegisterRequest request, String ipAddress) {
        checkEmailExisted(request.email());
        UserEntity user = toCreate(request);
        String accessToken = jwtService.createAccessToken(user, ROLE_USER);
        jwtService.createRefresh(user);
        return RegisterResponse.from(accessToken, user);
    }

    // login
    // get me
    // update me
    // forgot password
    // change password

    // deactive account

    private void checkEmailExisted(String email) {
        String emailTrim = normalize(email);
        userRepository.findByEmailIgnoreCase(emailTrim).ifPresent(u -> {
            throw AuthException.emailTaken();
        });
    }

    private UserEntity toCreate(RegisterRequest request) {
        UserEntity user = new UserEntity();
        user.setFullName(request.fullName());
        user.setEmail(normalize(request.email()));
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        return user;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

}
