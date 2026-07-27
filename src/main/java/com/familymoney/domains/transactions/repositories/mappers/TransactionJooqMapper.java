package com.familymoney.domains.transactions.repositories.mappers;

import com.familymoney.domains.transactions.repositories.entitites.TransactionEntity;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.TransactionId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.Transactions;
import java.time.OffsetDateTime;
import java.util.Objects;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
import org.jooq.Record;

public final class TransactionJooqMapper {

  private TransactionJooqMapper() {
    /* this class is not intended to be instantiated */
  }

  public static TransactionEntity toEntity(final Record r) {
    OffsetDateTime doneAt = Objects.requireNonNull(r.get(Transactions.TRANSACTIONS.DONE_AT));
    OffsetDateTime createdAt = Objects.requireNonNull(r.get(Transactions.TRANSACTIONS.CREATED_AT));
    OffsetDateTime updatedAt = Objects.requireNonNull(r.get(Transactions.TRANSACTIONS.UPDATED_AT));

    return new TransactionEntity(
        TransactionId.fromUuid(r.get(Transactions.TRANSACTIONS.ID)),
        Description.of(r.get(Transactions.TRANSACTIONS.DESCRIPTION)),
        GroupId.fromUuid(r.get(Transactions.TRANSACTIONS.GROUP_ID)),
        Money.of(
            r.get(Transactions.TRANSACTIONS.AMOUNT),
            Monetary.getCurrency(r.get(Transactions.TRANSACTIONS.CURRENCY_CODE))),
        UserId.fromUuid(r.get(Transactions.TRANSACTIONS.FROM_USER_ID)),
        UserId.fromUuid(r.get(Transactions.TRANSACTIONS.TO_USER_ID)),
        doneAt.toInstant(),
        createdAt.toInstant(),
        updatedAt.toInstant());
  }
}
