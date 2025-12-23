package com.familymoney.familymoney.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidCurrencyCodeValidator.class)
@Documented
public @interface ValidCurrencyCode {
  String message() default "Wrong ISO code for currency";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
