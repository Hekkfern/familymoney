package com.familymoney.familymoney.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

final class PasswordValidationConstants {

    public static final String VALIDATION_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,128}$";
    public static final String ERROR_MESSAGE = "Password must be between 12 and 128 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@\\$!%*?&)";
}

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Pattern(regexp = PasswordValidationConstants.VALIDATION_REGEX, message = PasswordValidationConstants.ERROR_MESSAGE)
public @interface Password {

    String message() default PasswordValidationConstants.ERROR_MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
