package com.familymoney.domains.transactions.repositories;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;

import com.familymoney.domains.transactions.repositories.dtos.CreateTransactionDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateTransactionDto;
import com.familymoney.domains.transactions.repositories.entitites.TransactionEntity;
import com.familymoney.domains.transactions.repositories.mappers.TransactionJooqMapper;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.TransactionId;
import com.familymoney.generated.tables.Transactions;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
            data.description().value(),
            data.groupId().value(),
            data.amount().getNumber().numberValue(java.math.BigDecimal.class),
            data.amount().getCurrency().getCurrencyCode(),
            data.lender().value(),
            data.borrower().value(),
            OffsetDateTime.ofInstant(data.doneAt(), DEFAULT_TIMEZONE_OFFSET))
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
    final BigDecimal amountVal =
        data.amount() != null
            ? data.amount().getNumber().numberValue(java.math.BigDecimal.class)
            : null;
    final String currencyVal =
        data.amount() != null ? data.amount().getCurrency().getCurrencyCode() : null;
    final String descriptionVal = data.description() != null ? data.description().value() : null;
    final UUID fromVal = data.from() != null ? data.from().value() : null;
    final UUID toVal = data.to() != null ? data.to().value() : null;
    final OffsetDateTime doneAtVal =
        data.doneAt() != null
            ? OffsetDateTime.ofInstant(data.doneAt(), DEFAULT_TIMEZONE_OFFSET)
            : null;

    final int rowsAffected =
        db.update(Transactions.TRANSACTIONS)
            .set(
                Transactions.TRANSACTIONS.AMOUNT,
                DSL.coalesce(DSL.val(amountVal), Transactions.TRANSACTIONS.AMOUNT))
            .set(
                Transactions.TRANSACTIONS.CURRENCY_CODE,
                DSL.coalesce(DSL.val(currencyVal), Transactions.TRANSACTIONS.CURRENCY_CODE))
            .set(
                Transactions.TRANSACTIONS.DESCRIPTION,
                DSL.coalesce(DSL.val(descriptionVal), Transactions.TRANSACTIONS.DESCRIPTION))
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
    final int rowsAffected =
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
    final Long total =
        db.selectCount()
            .from(Transactions.TRANSACTIONS)
            .where(Transactions.TRANSACTIONS.GROUP_ID.eq(groupId.value()))
            .fetchOne(0, Long.class);
    final long safeTotal = total != null ? total : 0L;

    final List<TransactionEntity> data =
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
