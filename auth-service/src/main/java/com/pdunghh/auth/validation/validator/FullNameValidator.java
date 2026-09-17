package com.pdunghh.auth.validation.validator;

import java.util.regex.Pattern;

import com.pdunghh.auth.validation.annotation.ValidFullName;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FullNameValidator implements ConstraintValidator<ValidFullName, String> {

    private final Pattern FULL_NAME_PATTERN = Pattern.compile("^[\\p{L}\\s.\\-']{2,100}$");

    public boolean isValid(String fullName, ConstraintValidatorContext context) {
        if (fullName == null) {
            return true;
        }
        return FULL_NAME_PATTERN.matcher(fullName).matches();
    }
}
