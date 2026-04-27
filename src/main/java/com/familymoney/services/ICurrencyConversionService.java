package com.familymoney.services;

import javax.money.CurrencyUnit;
import org.javamoney.moneta.Money;

public interface ICurrencyConversionService {

  Money convert(Money amount, CurrencyUnit targetCurrency);
}
