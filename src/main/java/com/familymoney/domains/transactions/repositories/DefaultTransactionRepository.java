package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.entitites.TransactionReference;
import com.familymoney.domains.transactions.services.data.TransactionType;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.generated.tables.Expenses;
import com.familymoney.generated.tables.Payments;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Retrieves transaction references across expenses and payments. */
@Repository
@RequiredArgsConstructor
public class DefaultTransactionRepository implements TransactionRepository {

  private final DSLContext db;

  @Transactional(readOnly = true)
  @Override
  public Page<TransactionReference> findAllByGroupId(
      final GroupId groupId, final Pageable pageable) {
    final Field<String> transactionType = DSL.inline(TransactionType.EXPENSE.name()).as("type");
    final Table<Record3<UUID, String, OffsetDateTime>> transactions =
        db.select(Expenses.EXPENSES.ID, transactionType, Expenses.EXPENSES.DONE_AT)
            .from(Expenses.EXPENSES)
            .where(Expenses.EXPENSES.GROUP_ID.eq(groupId.value()))
            .unionAll(
                db.select(
                        Payments.PAYMENTS.ID,
                        DSL.inline(TransactionType.PAYMENT.name()).as(transactionType),
                        Payments.PAYMENTS.DONE_AT)
                    .from(Payments.PAYMENTS)
                    .where(Payments.PAYMENTS.GROUP_ID.eq(groupId.value())))
            .asTable("transactions");

    final long total =
        db.select(DSL.count().cast(Long.class)).from(transactions).fetchSingle().value1();
    final Field<UUID> id = transactions.field(Expenses.EXPENSES.ID);
    final Field<String> type = transactions.field(transactionType);
    final Field<OffsetDateTime> doneAt = transactions.field(Expenses.EXPENSES.DONE_AT);
    final List<TransactionReference> data =
        db.select(id, type, doneAt)
            .from(transactions)
            .orderBy(doneAt.desc(), type.asc(), id.desc())
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetch(
                record ->
                    new TransactionReference(
                        record.value1(),
                        TransactionType.valueOf(record.value2()),
                        record.value3().toInstant()));
    return new PageImpl<>(data, pageable, total);
  }
}
