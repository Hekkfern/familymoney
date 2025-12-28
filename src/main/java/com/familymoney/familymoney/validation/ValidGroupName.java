package com.familymoney.familymoney.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Pattern(regexp = "^[A-Za-z0-9.-_ =><!?&%()/,]{0,64}$")
public @interface ValidGroupName {
  String message() default
      "Name must be alphanumeric, can contain some symbols, and have a max length of 64 characters";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
