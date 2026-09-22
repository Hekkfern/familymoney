package com.familymoney.domains.transactions.utils;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE;

import java.time.Instant;
import java.time.LocalDate;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.convert.MonetaryConversions;
import javax.money.convert.RateType;

/** Converts monetary amounts using an ECB historical exchange rate for a given instant. */
public class HistoricCurrencyConverter {

  private HistoricCurrencyConverter() {
    /* This utility class should not be instantiated */
  }

  /**
   * Converts an amount to the target currency using the ECB historical rate for the instant date.
   *
   * @param amount the amount to convert
   * @param toCurrency the currency to convert to
   * @param instant the instant that determines the historical exchange-rate date
   * @return the converted amount
   */
  public static MonetaryAmount convert(
      final MonetaryAmount amount, final CurrencyUnit toCurrency, final Instant instant) {
    final var fromCurrency = amount.getCurrency();
    if (fromCurrency.equals(toCurrency)) {
      return amount;
    }
    final var rateDate = LocalDate.ofInstant(instant, DEFAULT_TIMEZONE);
    final var query =
        ConversionQueryBuilder.of()
            .setBaseCurrency(fromCurrency)
            .setTermCurrency(toCurrency)
            .setRateTypes(RateType.HISTORIC)
            .set(LocalDate.class, rateDate)
            .build();
    final var provider = MonetaryConversions.getExchangeRateProvider("ECB-HIST");
    final var conversion = provider.getCurrencyConversion(query);
    return amount.with(conversion);
  }
}
