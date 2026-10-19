package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.services.data.TransactionType;
import com.familymoney.domains.transactions.services.data.TransactionData;

public final class TransactionDtoMapper {

  private TransactionDtoMapper() {
    /* this class is not intended to be instantiated */
  }

  public static TransactionDto toDto(final TransactionData data) {
    return new TransactionDto(TransactionType.EXPENSE, data.id());
  }
}
