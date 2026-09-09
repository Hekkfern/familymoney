package com.familymoney.domains.transactions.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotNegativeMoneyValidator.class)
@Documented
public @interface NotNegativeMoney {
  String message() default "Money amount must be zero or positive";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
