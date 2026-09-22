package com.familymoney.domains.transactions.repositories.mappers;

import com.familymoney.domains.transactions.repositories.entitites.ExpenseEntity;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.Expenses;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jooq.Record;

public final class ExpenseJooqMapper {

  private ExpenseJooqMapper() {
    /* this class is not intended to be instantiated */
  }

  public static ExpenseEntity toEntity(final Record r) {
    final OffsetDateTime doneAt = Objects.requireNonNull(r.get(Expenses.EXPENSES.DONE_AT));

    return new ExpenseEntity(
        ExpenseId.fromUuid(r.get(Expenses.EXPENSES.ID)),
        Description.of(r.get(Expenses.EXPENSES.DESCRIPTION)),
        GroupId.fromUuid(r.get(Expenses.EXPENSES.GROUP_ID)),
        UserId.fromUuid(r.get(Expenses.EXPENSES.CREATED_BY)),
        doneAt.toInstant());
  }
}
