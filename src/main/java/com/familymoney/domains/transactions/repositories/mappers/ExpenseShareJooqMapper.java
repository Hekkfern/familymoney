package com.familymoney.domains.transactions.repositories.mappers;

import com.familymoney.domains.transactions.repositories.entitites.ExpenseShareEntity;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.ExpenseShares;
import org.jooq.Record;

public final class ExpenseShareJooqMapper {

  private ExpenseShareJooqMapper() {
    /* this class is not intended to be instantiated */
  }

  public static ExpenseShareEntity toEntity(final Record r) {
    return new ExpenseShareEntity(
        UserId.fromUuid(r.get(ExpenseShares.EXPENSE_SHARES.USER_ID)),
        r.get(ExpenseShares.EXPENSE_SHARES.AMOUNT));
  }
}
