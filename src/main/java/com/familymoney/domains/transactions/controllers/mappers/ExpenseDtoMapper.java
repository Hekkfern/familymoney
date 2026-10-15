package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.ExpenseDto;
import com.familymoney.domains.transactions.services.data.ExpenseData;
import java.math.BigDecimal;
import org.javamoney.moneta.Money;

public final class ExpenseDtoMapper {

  private ExpenseDtoMapper() {
    /* this class is not intended to be instantiated */
  }

  public static ExpenseDto toDto(final ExpenseData data) {
    return new ExpenseDto(
        data.id().value(),
        data.groupId().value(),
        data.description().value(),
        data.totalAmount().getNumber().numberValueExact(BigDecimal.class),
        data.totalAmount().getCurrency().getCurrencyCode(),
        data.doneAt(),
        data.shares().entrySet().stream()
            .map(
                share ->
                    new ExpenseDto.ExpenseShareDto(
                        share.getKey().value(),
                        Money.of(share.getValue(), data.totalAmount().getCurrency())))
            .toList(),
        data.payers().entrySet().stream()
            .map(
                payer ->
                    new ExpenseDto.ExpensePayerDto(
                        payer.getKey().value(),
                        Money.of(payer.getValue(), data.totalAmount().getCurrency())))
            .toList());
  }
}
