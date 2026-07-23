package com.familymoney.domains.transactions.services.mappers;

import com.familymoney.domains.transactions.repositories.dtos.UpdateTransactionDto;
import com.familymoney.domains.transactions.services.data.UpdateTransactionData;

public final class UpdateTransactionDataMapper {

  private UpdateTransactionDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static UpdateTransactionDto toDbo(final UpdateTransactionData data) {
    return new UpdateTransactionDto(
        data.amount(), data.description(), data.from(), data.to(), data.doneAt());
  }
}
