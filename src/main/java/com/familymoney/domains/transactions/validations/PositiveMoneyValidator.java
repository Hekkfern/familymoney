package com.familymoney.domains.transactions.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.javamoney.moneta.Money;

public class PositiveMoneyValidator implements ConstraintValidator<PositiveMoney, Money> {

  @Override
  public boolean isValid(final Money value, final ConstraintValidatorContext context) {
    return value == null || value.isPositive();
  }
}
