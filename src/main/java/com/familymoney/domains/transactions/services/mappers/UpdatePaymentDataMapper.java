package com.familymoney.domains.transactions.services.mappers;

import com.familymoney.domains.transactions.repositories.dbos.UpdateExpenseDbo;
import com.familymoney.domains.transactions.repositories.dbos.UpdatePaymentDbo;
import com.familymoney.domains.transactions.services.data.UpdateExpenseData;
import com.familymoney.domains.transactions.services.data.UpdatePaymentData;

public final class UpdatePaymentDataMapper {

  private UpdatePaymentDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static UpdatePaymentDbo toDbo(final UpdatePaymentData data) {
    return new UpdatePaymentDbo(
        data.description(), data.doneAt(), data.amount(), data.from(), data.to());
  }
}
