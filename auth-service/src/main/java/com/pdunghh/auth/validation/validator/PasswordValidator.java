package com.pdunghh.auth.validation.validator;

import java.util.regex.Pattern;

import com.pdunghh.auth.validation.annotation.ValidPassword;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    private final Pattern PASSWORD_PATTERN = Pattern
            .compile("^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).{8,}$");

    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return true;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}
