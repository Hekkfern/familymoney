package com.familymoney.domains.transactions.services.mappers;

import com.familymoney.domains.transactions.repositories.dtos.UpdateExpenseDto;
import com.familymoney.domains.transactions.services.data.UpdateTransactionData;

public final class UpdateTransactionDataMapper {

  private UpdateTransactionDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static UpdateExpenseDto toDbo(final UpdateTransactionData data) {
    return new UpdateExpenseDto(
        data.amount(), data.description(), data.from(), data.to(), data.doneAt());
  }
}
