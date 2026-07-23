package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.services.data.TransactionData;

public final class TransactionMapper {

  private TransactionMapper() {
    /* this class is not intended to be instantiated */
  }

  public static TransactionDto toDto(final TransactionData data) {
    return new TransactionDto(
        data.id().value(),
        data.from().value(),
        data.to().value(),
        data.amount(),
        data.description().value(),
        data.doneAt());
  }
}
