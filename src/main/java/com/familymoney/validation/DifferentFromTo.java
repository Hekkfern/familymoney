package com.familymoney.validation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = DifferentFromToValidator.class)
@Target({TYPE})
@Retention(RUNTIME)
public @interface DifferentFromTo {
  String message() default "from and to must be different";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
