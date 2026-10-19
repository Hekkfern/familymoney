package com.familymoney.domains.transactions.services.mappers;

import com.familymoney.domains.transactions.repositories.dbos.UpdateExpenseDbo;
import com.familymoney.domains.transactions.services.data.UpdateExpenseData;

public final class UpdateExpenseDataMapper {

  private UpdateExpenseDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static UpdateExpenseDbo toDbo(final UpdateExpenseData data) {
    return new UpdateExpenseDbo(data.description(), data.doneAt(), data.shares(), data.payers());
  }
}
