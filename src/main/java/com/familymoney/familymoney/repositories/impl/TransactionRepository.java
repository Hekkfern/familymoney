package com.familymoney.familymoney.repositories.impl;

import com.familymoney.familymoney.generated.tables.Transactions;
import com.familymoney.familymoney.repositories.ITransactionRepository;
import com.familymoney.familymoney.repositories.dbos.TransactionDbo;
import com.familymoney.familymoney.repositories.dbos.UpdateTransactionDbo;
import com.familymoney.familymoney.repositories.mappers.TransactionJooqMapper;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.TransactionId;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.javamoney.moneta.Money;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TransactionRepository implements ITransactionRepository {

  private final DSLContext db;

  @Override
  public Optional<TransactionDbo> create(
      final String description,
      final GroupId groupId,
      final Money amount,
      final UserId from,
      final UserId to,
      final Instant doneAt) {
    assert amount.isGreaterThan(Money.zero(amount.getCurrency())) : "Amount must be positive";
    assert !from.equals(to) : "Lender and borrower must be different users";
    return db.insertInto(Transactions.TRANSACTIONS)
        .columns(
            Transactions.TRANSACTIONS.DESCRIPTION,
            Transactions.TRANSACTIONS.GROUP_ID,
            Transactions.TRANSACTIONS.AMOUNT,
            Transactions.TRANSACTIONS.CURRENCY_CODE,
            Transactions.TRANSACTIONS.FROM_USER_ID,
            Transactions.TRANSACTIONS.TO_USER_ID,
            Transactions.TRANSACTIONS.DONE_AT)
        .values(
            description,
            groupId.value(),
            amount.getNumber().numberValue(java.math.BigDecimal.class),
            amount.getCurrency().getCurrencyCode(),
            from.value(),
            to.value(),
            OffsetDateTime.ofInstant(doneAt, ZoneOffset.UTC))
        .returning(
            Transactions.TRANSACTIONS.ID,
            Transactions.TRANSACTIONS.DESCRIPTION,
            Transactions.TRANSACTIONS.GROUP_ID,
            Transactions.TRANSACTIONS.CURRENCY_CODE,
            Transactions.TRANSACTIONS.FROM_USER_ID,
            Transactions.TRANSACTIONS.TO_USER_ID,
            Transactions.TRANSACTIONS.DONE_AT,
            Transactions.TRANSACTIONS.CREATED_AT,
            Transactions.TRANSACTIONS.UPDATED_AT)
        .fetchOptional()
        .map(TransactionJooqMapper::toDbo);
  }

  @Override
  public boolean updateById(final TransactionId id, final UpdateTransactionDbo data) {
    val amountVal =
        data.getAmount() != null
            ? data.getAmount().getNumber().numberValue(java.math.BigDecimal.class)
            : null;
    val currencyVal =
        data.getAmount() != null ? data.getAmount().getCurrency().getCurrencyCode() : null;
    val fromVal = data.getFrom() != null ? data.getFrom().value() : null;
    val toVal = data.getTo() != null ? data.getTo().value() : null;
    val doneAtVal =
        data.getDoneAt() != null
            ? OffsetDateTime.ofInstant(data.getDoneAt(), ZoneOffset.UTC)
            : null;

    val rowsAffected =
        db.update(Transactions.TRANSACTIONS)
            .set(
                Transactions.TRANSACTIONS.AMOUNT,
                DSL.coalesce(DSL.val(amountVal), Transactions.TRANSACTIONS.AMOUNT))
            .set(
                Transactions.TRANSACTIONS.CURRENCY_CODE,
                DSL.coalesce(DSL.val(currencyVal), Transactions.TRANSACTIONS.CURRENCY_CODE))
            .set(
                Transactions.TRANSACTIONS.DESCRIPTION,
                DSL.coalesce(DSL.val(data.getDescription()), Transactions.TRANSACTIONS.DESCRIPTION))
            .set(
                Transactions.TRANSACTIONS.FROM_USER_ID,
                DSL.coalesce(DSL.val(fromVal), Transactions.TRANSACTIONS.FROM_USER_ID))
            .set(
                Transactions.TRANSACTIONS.TO_USER_ID,
                DSL.coalesce(DSL.val(toVal), Transactions.TRANSACTIONS.TO_USER_ID))
            .set(
                Transactions.TRANSACTIONS.DONE_AT,
                DSL.coalesce(DSL.val(doneAtVal), Transactions.TRANSACTIONS.DONE_AT))
            .where(Transactions.TRANSACTIONS.ID.eq(id.value()))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public boolean deleteById(final TransactionId id) {
    val rowsAffected =
        db.deleteFrom(Transactions.TRANSACTIONS)
            .where(Transactions.TRANSACTIONS.ID.eq(id.value()))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public Optional<TransactionDbo> findById(final TransactionId id) {
    return db.select(
            Transactions.TRANSACTIONS.ID,
            Transactions.TRANSACTIONS.DESCRIPTION,
            Transactions.TRANSACTIONS.GROUP_ID,
            Transactions.TRANSACTIONS.CURRENCY_CODE,
            Transactions.TRANSACTIONS.FROM_USER_ID,
            Transactions.TRANSACTIONS.TO_USER_ID,
            Transactions.TRANSACTIONS.DONE_AT,
            Transactions.TRANSACTIONS.CREATED_AT,
            Transactions.TRANSACTIONS.UPDATED_AT)
        .from(Transactions.TRANSACTIONS)
        .where(Transactions.TRANSACTIONS.ID.eq(id.value()))
        .fetchOptional()
        .map(TransactionJooqMapper::toDbo);
  }

  @Override
  public Page<TransactionDbo> findAllByGroupId(final GroupId groupId, final Pageable pageable) {
    val total =
        db.selectCount()
            .from(Transactions.TRANSACTIONS)
            .where(Transactions.TRANSACTIONS.GROUP_ID.eq(groupId.value()))
            .fetchOne(0, Long.class);
    val safeTotal = total != null ? total : 0L;

    val data =
        db.select(
                Transactions.TRANSACTIONS.ID,
                Transactions.TRANSACTIONS.DESCRIPTION,
                Transactions.TRANSACTIONS.GROUP_ID,
                Transactions.TRANSACTIONS.CURRENCY_CODE,
                Transactions.TRANSACTIONS.FROM_USER_ID,
                Transactions.TRANSACTIONS.TO_USER_ID,
                Transactions.TRANSACTIONS.DONE_AT,
                Transactions.TRANSACTIONS.CREATED_AT,
                Transactions.TRANSACTIONS.UPDATED_AT)
            .from(Transactions.TRANSACTIONS)
            .where(Transactions.TRANSACTIONS.GROUP_ID.eq(groupId.value()))
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetch()
            .map(TransactionJooqMapper::toDbo);

    return new PageImpl<>(data, pageable, safeTotal);
  }
}
