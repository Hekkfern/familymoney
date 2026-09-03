package com.familymoney.domains.transactions.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultCurrencyConversionServiceTest {

  @InjectMocks private DefaultCurrencyConversionService currencyConversionService;

  @Test
  void convert_same_currency_returns_same_amount() {
    final Money amount = Money.of(100, "USD");
    final Money convertedAmount = currencyConversionService.convert(amount, amount.getCurrency());
    assertEquals(amount, convertedAmount);
  }

  @Test
  void convert_different_currency_returns_correct_amount() {
    final CurrencyUnit originalCurrency = Monetary.getCurrency("USD");
    final CurrencyUnit targetCurrency = Monetary.getCurrency("EUR");
    final Money amount = Money.of(100, originalCurrency);
    final Money convertedAmount = currencyConversionService.convert(amount, targetCurrency);
    assertEquals(targetCurrency, convertedAmount.getCurrency());
    assertNotEquals(100, convertedAmount.getNumber().doubleValue());
  }

  @Test
  void convert_zero_amount_returns_zero_in_target_currency() {
    final CurrencyUnit originalCurrency = Monetary.getCurrency("USD");
    final CurrencyUnit targetCurrency = Monetary.getCurrency("EUR");
    final Money amount = Money.of(0, originalCurrency);
    final Money convertedAmount = currencyConversionService.convert(amount, targetCurrency);
    assertEquals(targetCurrency, convertedAmount.getCurrency());
    assertEquals(0, convertedAmount.getNumber().doubleValue());
  }

  @Test
  void convert_negative_amount_returns_negative_in_target_currency() {
    final CurrencyUnit originalCurrency = Monetary.getCurrency("USD");
    final CurrencyUnit targetCurrency = Monetary.getCurrency("EUR");
    final Money amount = Money.of(-50, originalCurrency);
    final Money convertedAmount = currencyConversionService.convert(amount, targetCurrency);
    assertEquals(targetCurrency, convertedAmount.getCurrency());
    assertTrue(convertedAmount.getNumber().doubleValue() < 0);
    assertTrue(convertedAmount.isNegative());
  }
}
