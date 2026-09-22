package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.ExpenseDto;
import com.familymoney.domains.transactions.services.data.TransactionData;

public final class TransactionMapper {

  private TransactionMapper() {
    /* this class is not intended to be instantiated */
  }

  public static ExpenseDto toDto(final TransactionData data) {
    return new ExpenseDto(
        data.id().value(),
        data.from().value(),
        data.to().value(),
        data.amount(),
        data.description().value(),
        data.doneAt());
  }
}
