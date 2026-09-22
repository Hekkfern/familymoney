package com.familymoney.domains.transactions.utils;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.convert.ConversionQuery;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.MonetaryConversions;

/** Converts monetary amounts with the latest exchange rate available from the ECB provider. */
public class RealtimeCurrencyConverter {

  private static final String CURRENT_RATE_PROVIDER = "ECB";

  private RealtimeCurrencyConverter() {
    /* This utility class should not be instantiated */
  }

  /**
   * Converts an amount from its source currency to the target currency using the current ECB rate.
   *
   * @param amount the amount to convert
   * @param toCurrency the currency to convert to
   * @return the converted amount
   * @throws IllegalArgumentException if the source currency does not match the amount currency
   */
  public static MonetaryAmount convert(final MonetaryAmount amount, final CurrencyUnit toCurrency) {
    final var fromCurrency = amount.getCurrency();
    if (fromCurrency.equals(toCurrency)) {
      return amount;
    }
    final ConversionQuery query =
        ConversionQueryBuilder.of()
            .setBaseCurrency(fromCurrency)
            .setTermCurrency(toCurrency)
            .build();
    final ExchangeRateProvider provider =
        MonetaryConversions.getExchangeRateProvider(CURRENT_RATE_PROVIDER);
    final CurrencyConversion conversion = provider.getCurrencyConversion(query);
    return amount.with(conversion);
  }
}
