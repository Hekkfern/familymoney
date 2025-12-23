package com.familymoney.familymoney.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Currency;
import java.util.Set;

public class ValidCurrencyCodeValidator implements ConstraintValidator<ValidCurrencyCode, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    boolean containsIsoCode = false;

    Set<Currency> currencies = Currency.getAvailableCurrencies();
    try {
      containsIsoCode = currencies.contains(Currency.getInstance(value));
    } catch (IllegalArgumentException _) {
    }
    return containsIsoCode;
  }
}
