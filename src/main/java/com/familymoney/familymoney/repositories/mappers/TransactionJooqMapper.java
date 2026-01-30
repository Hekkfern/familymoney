package com.familymoney.familymoney.repositories.mappers;

import com.familymoney.familymoney.generated.tables.Transactions;
import com.familymoney.familymoney.repositories.dbos.TransactionDbo;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.TransactionId;
import com.familymoney.familymoney.types.UserId;
import java.time.OffsetDateTime;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
import org.jooq.Record;

public final class TransactionJooqMapper {

  private TransactionJooqMapper() {}

  public static TransactionDbo toDbo(final Record r) {
    OffsetDateTime doneAt = r.get(Transactions.TRANSACTIONS.DONE_AT);
    OffsetDateTime createdAt = r.get(Transactions.TRANSACTIONS.CREATED_AT);
    OffsetDateTime updatedAt = r.get(Transactions.TRANSACTIONS.UPDATED_AT);

    return TransactionDbo.builder()
        .id(TransactionId.fromUuid(r.get(Transactions.TRANSACTIONS.ID)))
        .description(r.get(Transactions.TRANSACTIONS.DESCRIPTION))
        .groupId(GroupId.fromUuid(r.get(Transactions.TRANSACTIONS.GROUP_ID)))
        .amount(
            Money.of(
                r.get(Transactions.TRANSACTIONS.AMOUNT),
                Monetary.getCurrency(r.get(Transactions.TRANSACTIONS.CURRENCY_CODE))))
        .from(UserId.fromUuid(r.get(Transactions.TRANSACTIONS.FROM_USER_ID)))
        .to(UserId.fromUuid(r.get(Transactions.TRANSACTIONS.TO_USER_ID)))
        .doneAt(doneAt != null ? doneAt.toInstant() : null)
        .createdAt(createdAt != null ? createdAt.toInstant() : null)
        .updatedAt(updatedAt != null ? updatedAt.toInstant() : null)
        .build();
  }
}
