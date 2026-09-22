package com.familymoney.domains.transactions.repositories.mappers;

import com.familymoney.domains.transactions.repositories.entitites.ExpensePaymentEntity;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.ExpensePayments;
import org.jooq.Record;

public final class ExpensePaymentJooqMapper {

  private ExpensePaymentJooqMapper() {
    /* this class is not intended to be instantiated */
  }

  public static ExpensePaymentEntity toEntity(final Record r) {
    return new ExpensePaymentEntity(
        ExpenseId.fromUuid(r.get(ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID)),
        UserId.fromUuid(r.get(ExpensePayments.EXPENSE_PAYMENTS.USER_ID)),
        r.get(ExpensePayments.EXPENSE_PAYMENTS.AMOUNT));
  }
}
