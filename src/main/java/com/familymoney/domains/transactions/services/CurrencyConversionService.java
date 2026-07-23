package com.familymoney.domains.transactions.services;

import javax.money.CurrencyUnit;
import javax.money.convert.MonetaryConversions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyConversionService implements ICurrencyConversionService {

  @Override
  public Money convert(Money amount, CurrencyUnit targetCurrency) {
    val conv = MonetaryConversions.getConversion(targetCurrency);
    return amount.with(conv);
  }
}
