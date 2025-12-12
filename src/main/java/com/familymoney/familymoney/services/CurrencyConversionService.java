package com.familymoney.familymoney.services;

import javax.money.CurrencyUnit;
import javax.money.convert.MonetaryConversions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyConversionService implements ICurrencyConversionService {
  @Override
  public @NonNull Money convert(@NonNull Money amount, @NonNull CurrencyUnit targetCurrency) {
    val conv = MonetaryConversions.getConversion(targetCurrency);
    return amount.with(conv);
  }
}
