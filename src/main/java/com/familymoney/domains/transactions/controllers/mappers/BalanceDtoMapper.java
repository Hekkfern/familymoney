package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.BalanceDto;
import com.familymoney.domains.transactions.services.data.BalanceData;
import java.math.BigDecimal;

public final class BalanceDtoMapper {

  private BalanceDtoMapper() {
    /* this class is not intended to be instantiated */
  }

  public static BalanceDto toDto(final BalanceData data) {
    return new BalanceDto(
        data.creditor().value(),
        data.debitor().value(),
        data.amount().getNumber().numberValueExact(BigDecimal.class),
        data.amount().getCurrency().getCurrencyCode());
  }
}
