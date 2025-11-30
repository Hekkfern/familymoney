package com.familymoney.familymoney.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

final class JwtTokenValidationConstants {

  public static final String VALIDATION_REGEX =
      "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";
  public static final String ERROR_MESSAGE = "Invalid token format";
}

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Pattern(
    regexp = JwtTokenValidationConstants.VALIDATION_REGEX,
    message = JwtTokenValidationConstants.ERROR_MESSAGE)
public @interface JwtToken {

  String message() default JwtTokenValidationConstants.ERROR_MESSAGE;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
