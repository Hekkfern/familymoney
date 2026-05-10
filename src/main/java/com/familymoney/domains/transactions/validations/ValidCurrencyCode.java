package com.familymoney.domains.transactions.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidCurrencyCodeValidator.class)
@Documented
public @interface ValidCurrencyCode {
  String message() default "Wrong ISO code for currency";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
