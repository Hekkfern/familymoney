package com.familymoney.domains.transactions.services;

import javax.money.CurrencyUnit;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.MonetaryConversions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultCurrencyConversionService implements CurrencyConversionService {

  @Override
  public Money convert(Money amount, CurrencyUnit targetCurrency) {
    final CurrencyConversion conv = MonetaryConversions.getConversion(targetCurrency);
    return amount.with(conv);
  }
}
