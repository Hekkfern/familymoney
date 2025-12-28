package com.familymoney.familymoney.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Pattern(regexp = "^[A-Za-z0-9]{64}$")
public @interface ValidShareGroupToken {
  String message() default "Invalid token format";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
