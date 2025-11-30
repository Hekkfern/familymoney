package com.familymoney.familymoney.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

final class EmailVerificationTokenValidationConstants {
  public static final String VALIDATION_REGEX = "^[A-Za-z0-9]{64}$";
  public static final String ERROR_MESSAGE = "Invalid token format";
}

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Pattern(
    regexp = EmailVerificationTokenValidationConstants.VALIDATION_REGEX,
    message = EmailVerificationTokenValidationConstants.ERROR_MESSAGE)
public @interface EmailVerificationToken {

  String message() default EmailVerificationTokenValidationConstants.ERROR_MESSAGE;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
