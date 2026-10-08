package com.pdunghh.shared.annotation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    // Ít nhất 1 chữ hoa, 1 chữ thường, 1 ký tự đặc biệt, độ dài từ 8 đến 50 ký tự
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_]).{8,50}$";
    private static final Pattern PATTERN = Pattern.compile(PASSWORD_PATTERN);

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return PATTERN.matcher(password).matches();
    }
}
