package com.familymoney.familymoney.repositories.impl;

import com.familymoney.familymoney.generated.tables.Transactions;
import com.familymoney.familymoney.repositories.ITransactionRepository;
import com.familymoney.familymoney.repositories.dtos.CreateTransactionDto;
import com.familymoney.familymoney.repositories.dtos.UpdateTransactionDto;
import com.familymoney.familymoney.repositories.entities.TransactionEntity;
import com.familymoney.familymoney.repositories.mappers.TransactionJooqMapper;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.TransactionId;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
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
  public Optional<TransactionEntity> create(final CreateTransactionDto data) {

    return db.insertInto(Transactions.TRANSACTIONS)
        .columns(
            Transactions.TRANSACTIONS.ID,
            Transactions.TRANSACTIONS.DESCRIPTION,
            Transactions.TRANSACTIONS.GROUP_ID,
            Transactions.TRANSACTIONS.AMOUNT,
            Transactions.TRANSACTIONS.CURRENCY_CODE,
            Transactions.TRANSACTIONS.FROM_USER_ID,
            Transactions.TRANSACTIONS.TO_USER_ID,
            Transactions.TRANSACTIONS.DONE_AT)
        .values(
            data.id().value(),
            data.description(),
            data.groupId().value(),
            data.amount().getNumber().numberValue(java.math.BigDecimal.class),
            data.amount().getCurrency().getCurrencyCode(),
            data.lender().value(),
            data.borrower().value(),
            OffsetDateTime.ofInstant(data.doneAt(), ZoneOffset.UTC))
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
        .map(TransactionJooqMapper::toEntity);
  }

  @Override
  public boolean updateById(final TransactionId id, final UpdateTransactionDto data) {
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
  public Optional<TransactionEntity> findById(final TransactionId id) {
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
        .map(TransactionJooqMapper::toEntity);
  }

  @Override
  public Page<TransactionEntity> findAllByGroupId(final GroupId groupId, final Pageable pageable) {
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
            .map(TransactionJooqMapper::toEntity);

    return new PageImpl<>(data, pageable, safeTotal);
  }
}
