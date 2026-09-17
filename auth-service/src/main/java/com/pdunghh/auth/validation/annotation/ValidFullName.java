package com.pdunghh.auth.validation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.pdunghh.auth.service.AuthService;
import com.pdunghh.auth.validation.validator.FullNameValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = FullNameValidator.class)
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFullName {

    String message() default AuthService.FULL_NAME_INVALID;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
