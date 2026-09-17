package com.pdunghh.auth.validation.validator;

import java.util.regex.Pattern;

import com.pdunghh.auth.validation.annotation.ValidPhone;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {
    private final Pattern PHONE_PATTERN = Pattern.compile("^(0|\\+84)(2|3|5|7|8|9)\\d{8}$");

    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null) {
            return true;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }
}
