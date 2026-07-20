package com.familymoney.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.familymoney.domains.transactions.services.CurrencyConversionService;
import javax.money.Monetary;
import lombok.val;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CurrencyConversionServiceTest {

  @InjectMocks private CurrencyConversionService currencyConversionService;

  @Test
  void convert_same_currency_returns_same_amount() {
    val amount = Money.of(100, "USD");
    val convertedAmount = currencyConversionService.convert(amount, amount.getCurrency());
    assertEquals(amount, convertedAmount);
  }

  @Test
  void convert_different_currency_returns_correct_amount() {
    val originalCurrency = Monetary.getCurrency("USD");
    val targetCurrency = Monetary.getCurrency("EUR");
    val amount = Money.of(100, originalCurrency);
    val convertedAmount = currencyConversionService.convert(amount, targetCurrency);
    assertEquals(targetCurrency, convertedAmount.getCurrency());
    assertNotEquals(100, convertedAmount.getNumber().doubleValue());
  }

  @Test
  void convert_zero_amount_returns_zero_in_target_currency() {
    val originalCurrency = Monetary.getCurrency("USD");
    val targetCurrency = Monetary.getCurrency("EUR");
    val amount = Money.of(0, originalCurrency);
    val convertedAmount = currencyConversionService.convert(amount, targetCurrency);
    assertEquals(targetCurrency, convertedAmount.getCurrency());
    assertEquals(0, convertedAmount.getNumber().doubleValue());
  }

  @Test
  void convert_negative_amount_returns_negative_in_target_currency() {
    val originalCurrency = Monetary.getCurrency("USD");
    val targetCurrency = Monetary.getCurrency("EUR");
    val amount = Money.of(-50, originalCurrency);
    val convertedAmount = currencyConversionService.convert(amount, targetCurrency);
    assertEquals(targetCurrency, convertedAmount.getCurrency());
    assertTrue(convertedAmount.getNumber().doubleValue() < 0);
    assertTrue(convertedAmount.isNegative());
  }
}
