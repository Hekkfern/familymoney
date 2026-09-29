package com.familymoney.domains.transactions.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
// 1 to 64 allowed characters, at least one of them not a space
@Pattern(
    regexp = "^(?=.*[^ ])[A-Za-z0-9._ =><!?&%()/,-]{1,64}$",
    message =
        "Name must be alphanumeric, can contain some symbols, and have a max length of 64 characters")
public @interface ValidGroupName {
  String message() default
      "Name must be alphanumeric, can contain some symbols, and have a max length of 64 characters";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
