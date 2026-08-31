package com.familymoney.domains.transactions.services;

import javax.money.CurrencyUnit;
import org.javamoney.moneta.Money;

public interface CurrencyConversionService {

  Money convert(Money amount, CurrencyUnit targetCurrency);
}
