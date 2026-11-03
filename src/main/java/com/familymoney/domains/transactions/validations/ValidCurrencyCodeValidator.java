package com.familymoney.domains.transactions.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Currency;
import org.jspecify.annotations.Nullable;

public class ValidCurrencyCodeValidator implements ConstraintValidator<ValidCurrencyCode, String> {

  @Override
  public boolean isValid(final @Nullable String value, final ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    try {
      Currency.getInstance(value);
      return true;
    } catch (IllegalArgumentException _) {
      return false;
    }
  }
}
