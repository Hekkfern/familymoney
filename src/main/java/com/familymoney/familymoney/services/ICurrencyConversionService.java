package com.familymoney.familymoney.services;

import javax.money.CurrencyUnit;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.NonNull;

public interface ICurrencyConversionService {

  @NonNull Money convert(@NonNull Money amount, @NonNull CurrencyUnit targetCurrency);
}
