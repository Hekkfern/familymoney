package com.familymoney.familymoney.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

final class UsernameValidationConstants {

    public static final String VALIDATION_REGEX = "^[a-z][a-z0-9_-]{2,31}$";
    public static final String ERROR_MESSAGE = "Name must be alphanumeric, '-' and '_', and have a length between 3 and 32 characters.";
}

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Pattern(regexp = UsernameValidationConstants.VALIDATION_REGEX, message = UsernameValidationConstants.ERROR_MESSAGE)
public @interface Username {

    String message() default UsernameValidationConstants.ERROR_MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
