package com.familymoney.domains.transactions.repositories;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;

import com.familymoney.domains.transactions.exceptions.ExpenseNotFoundException;
import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.repositories.dtos.CreateExpenseDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateExpenseDto;
import com.familymoney.domains.transactions.repositories.entitites.FullExpenseEntity;
import com.familymoney.domains.transactions.repositories.mappers.ExpenseJooqMapper;
import com.familymoney.domains.transactions.repositories.mappers.ExpensePaymentJooqMapper;
import com.familymoney.domains.transactions.repositories.mappers.ExpenseShareJooqMapper;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.Keys;
import com.familymoney.generated.tables.ExpensePayments;
import com.familymoney.generated.tables.ExpenseShares;
import com.familymoney.generated.tables.Expenses;
import com.familymoney.generated.tables.Groups;
import com.familymoney.utils.ConstraintViolationUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DefaultExpenseRepository implements ExpenseRepository {

  private final DSLContext db;

  @Transactional
  @Override
  public void create(final CreateExpenseDto dto) {
    try {
      db.insertInto(Expenses.EXPENSES)
          .columns(
              Expenses.EXPENSES.ID,
              Expenses.EXPENSES.DESCRIPTION,
              Expenses.EXPENSES.GROUP_ID,
              Expenses.EXPENSES.DONE_AT,
              Expenses.EXPENSES.CREATED_BY)
          .values(
              dto.id().value(),
              dto.description().value(),
              dto.groupId().value(),
              OffsetDateTime.ofInstant(dto.doneAt(), DEFAULT_TIMEZONE_OFFSET),
              dto.createdBy().value())
          .execute();
      insertShares(dto.id(), dto.shares());
      insertPayments(dto.id(), dto.payers());
    } catch (final DataIntegrityViolationException exception) {
      throw toDomainException(exception, dto.groupId(), dto.createdBy());
    }
  }

  @Transactional
  @Override
  public void updateById(final ExpenseId id, final UpdateExpenseDto dto) {
    final Map<Field<?>, Object> values = new LinkedHashMap<>();
    values.put(Expenses.EXPENSES.UPDATED_AT, DSL.currentOffsetDateTime());
    if (dto.description() != null) {
      values.put(Expenses.EXPENSES.DESCRIPTION, dto.description().value());
    }
    if (dto.doneAt() != null) {
      values.put(
          Expenses.EXPENSES.DONE_AT,
          OffsetDateTime.ofInstant(dto.doneAt(), DEFAULT_TIMEZONE_OFFSET));
    }

    final int expensesUpdated =
        db.update(Expenses.EXPENSES)
            .set(values)
            .where(Expenses.EXPENSES.ID.eq(id.value()))
            .execute();
    if (expensesUpdated == 0) {
      final String msg = "Expense with ID: %s does not exist".formatted(id.value());
      log.info(msg);
      throw new ExpenseNotFoundException(msg);
    }

    final Map<UserId, BigDecimal> shares = dto.shares();
    final Map<UserId, BigDecimal> payers = dto.payers();
    if (shares != null && payers != null) {
      replaceSharesAndPayers(id, shares, payers);
    }
  }

  private void replaceSharesAndPayers(
      final ExpenseId id,
      final Map<UserId, BigDecimal> shares,
      final Map<UserId, BigDecimal> payers) {
    try {
      db.deleteFrom(ExpenseShares.EXPENSE_SHARES)
          .where(ExpenseShares.EXPENSE_SHARES.EXPENSE_ID.eq(id.value()))
          .execute();
      insertShares(id, shares);
      db.deleteFrom(ExpensePayments.EXPENSE_PAYMENTS)
          .where(ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID.eq(id.value()))
          .execute();
      insertPayments(id, payers);
    } catch (final DataIntegrityViolationException exception) {
      throw toDomainException(exception, null, null);
    }
  }

  private static RuntimeException toDomainException(
      final DataIntegrityViolationException exception,
      final @Nullable GroupId groupId,
      final @Nullable UserId createdBy) {
    final boolean isGroupMissing =
        groupId != null
            && ConstraintViolationUtils.isConstraintViolated(
                exception, Keys.EXPENSES__EXPENSES_GROUP_ID_FKEY);
    if (isGroupMissing) {
      final String msg = "Group with ID: %s does not exist".formatted(groupId.value());
      log.info(msg);
      return new GroupNotFoundException(msg, exception);
    }
    final boolean isCreatorMissing =
        createdBy != null
            && ConstraintViolationUtils.isConstraintViolated(
                exception, Keys.EXPENSES__EXPENSES_CREATED_BY_FKEY);
    if (isCreatorMissing) {
      final String msg = "Creator with ID: %s does not exist".formatted(createdBy.value());
      log.info(msg);
      return new UserNotFoundException(msg, exception);
    }
    if (ConstraintViolationUtils.isConstraintViolated(
        exception, Keys.EXPENSE_SHARES__EXPENSE_SHARES_USER_ID_FKEY)) {
      final String msg = "A user in the expense shares does not exist";
      log.info(msg);
      return new UserNotFoundException(msg, exception);
    }
    if (ConstraintViolationUtils.isConstraintViolated(
        exception, Keys.EXPENSE_PAYMENTS__EXPENSE_PAYMENTS_USER_ID_FKEY)) {
      final String msg = "A user in the expense payers does not exist";
      log.info(msg);
      return new UserNotFoundException(msg, exception);
    }
    return exception;
  }

  @Transactional
  @Override
  public void deleteById(final ExpenseId id) {
    db.deleteFrom(ExpenseShares.EXPENSE_SHARES)
        .where(ExpenseShares.EXPENSE_SHARES.EXPENSE_ID.eq(id.value()))
        .execute();
    db.deleteFrom(ExpensePayments.EXPENSE_PAYMENTS)
        .where(ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID.eq(id.value()))
        .execute();
    final int rowsAffected =
        db.deleteFrom(Expenses.EXPENSES).where(Expenses.EXPENSES.ID.eq(id.value())).execute();
    if (rowsAffected != 1) {
      log.warn("Could not delete expense ID: {}", id.value());
    }
  }

  @Transactional
  @Override
  public int deleteExpensesOfPurgeableGroups(final Instant deletedBefore, final int batchSize) {
    final List<UUID> expenseIds =
        db.select(Expenses.EXPENSES.ID)
            .from(Expenses.EXPENSES)
            .join(Groups.GROUPS)
            .on(Groups.GROUPS.ID.eq(Expenses.EXPENSES.GROUP_ID))
            .where(Groups.GROUPS.DELETED_AT.lt(deletedBefore.atOffset(DEFAULT_TIMEZONE_OFFSET)))
            .limit(batchSize)
            .fetch(Expenses.EXPENSES.ID);
    if (expenseIds.isEmpty()) {
      return 0;
    }

    db.deleteFrom(ExpenseShares.EXPENSE_SHARES)
        .where(ExpenseShares.EXPENSE_SHARES.EXPENSE_ID.in(expenseIds))
        .execute();
    db.deleteFrom(ExpensePayments.EXPENSE_PAYMENTS)
        .where(ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID.in(expenseIds))
        .execute();
    return db.deleteFrom(Expenses.EXPENSES).where(Expenses.EXPENSES.ID.in(expenseIds)).execute();
  }

  @Override
  public Optional<FullExpenseEntity> findById(final ExpenseId id) {
    return db.select(
            Expenses.EXPENSES.ID,
            Expenses.EXPENSES.DESCRIPTION,
            Expenses.EXPENSES.GROUP_ID,
            Expenses.EXPENSES.DONE_AT,
            DSL.multiset(
                    db.select(
                            ExpenseShares.EXPENSE_SHARES.EXPENSE_ID,
                            ExpenseShares.EXPENSE_SHARES.USER_ID,
                            ExpenseShares.EXPENSE_SHARES.AMOUNT)
                        .from(ExpenseShares.EXPENSE_SHARES)
                        .where(ExpenseShares.EXPENSE_SHARES.EXPENSE_ID.eq(id.value())))
                .as("shares"),
            DSL.multiset(
                    db.select(
                            ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID,
                            ExpensePayments.EXPENSE_PAYMENTS.USER_ID,
                            ExpensePayments.EXPENSE_PAYMENTS.AMOUNT)
                        .from(ExpensePayments.EXPENSE_PAYMENTS)
                        .where(ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID.eq(id.value())))
                .as("payments"))
        .from(Expenses.EXPENSES)
        .where(Expenses.EXPENSES.ID.eq(id.value()))
        .fetchOptional(
            record ->
                new FullExpenseEntity(
                    ExpenseJooqMapper.toEntity(record),
                    record.value5().map(ExpenseShareJooqMapper::toEntity),
                    record.value6().map(ExpensePaymentJooqMapper::toEntity)));
  }

  @Transactional(readOnly = true)
  @Override
  public Page<FullExpenseEntity> findAllByGroupId(
      final GroupId groupId, final int page, final int size) {
    final PageRequest pageable = PageRequest.of(page, size);
    final Long total =
        db.selectCount()
            .from(Expenses.EXPENSES)
            .where(Expenses.EXPENSES.GROUP_ID.eq(groupId.value()))
            .fetchOne(0, Long.class);
    final long safeTotal = total != null ? total : 0L;
    final List<FullExpenseEntity> data =
        db.select(
                Expenses.EXPENSES.ID,
                Expenses.EXPENSES.DESCRIPTION,
                Expenses.EXPENSES.GROUP_ID,
                Expenses.EXPENSES.DONE_AT,
                DSL.multiset(
                        db.select(
                                ExpenseShares.EXPENSE_SHARES.EXPENSE_ID,
                                ExpenseShares.EXPENSE_SHARES.USER_ID,
                                ExpenseShares.EXPENSE_SHARES.AMOUNT)
                            .from(ExpenseShares.EXPENSE_SHARES)
                            .where(
                                ExpenseShares.EXPENSE_SHARES.EXPENSE_ID.eq(Expenses.EXPENSES.ID)))
                    .as("shares"),
                DSL.multiset(
                        db.select(
                                ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID,
                                ExpensePayments.EXPENSE_PAYMENTS.USER_ID,
                                ExpensePayments.EXPENSE_PAYMENTS.AMOUNT)
                            .from(ExpensePayments.EXPENSE_PAYMENTS)
                            .where(
                                ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID.eq(
                                    Expenses.EXPENSES.ID)))
                    .as("payments"))
            .from(Expenses.EXPENSES)
            .where(Expenses.EXPENSES.GROUP_ID.eq(groupId.value()))
            .orderBy(Expenses.EXPENSES.DONE_AT.desc(), Expenses.EXPENSES.ID.desc())
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetch(
                record ->
                    new FullExpenseEntity(
                        ExpenseJooqMapper.toEntity(record),
                        record.value5().map(ExpenseShareJooqMapper::toEntity),
                        record.value6().map(ExpensePaymentJooqMapper::toEntity)));
    return new PageImpl<>(data, pageable, safeTotal);
  }

  private void insertShares(final ExpenseId expenseId, final Map<UserId, BigDecimal> shares) {
    db.insertInto(
            ExpenseShares.EXPENSE_SHARES,
            ExpenseShares.EXPENSE_SHARES.EXPENSE_ID,
            ExpenseShares.EXPENSE_SHARES.USER_ID,
            ExpenseShares.EXPENSE_SHARES.AMOUNT)
        .valuesOfRows(
            shares.entrySet().stream()
                .map(
                    shareEntry ->
                        DSL.row(
                            expenseId.value(), shareEntry.getKey().value(), shareEntry.getValue()))
                .toList())
        .execute();
  }

  private void insertPayments(final ExpenseId expenseId, final Map<UserId, BigDecimal> payers) {
    db.insertInto(
            ExpensePayments.EXPENSE_PAYMENTS,
            ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID,
            ExpensePayments.EXPENSE_PAYMENTS.USER_ID,
            ExpensePayments.EXPENSE_PAYMENTS.AMOUNT)
        .valuesOfRows(
            payers.entrySet().stream()
                .map(
                    payerEntry ->
                        DSL.row(
                            expenseId.value(), payerEntry.getKey().value(), payerEntry.getValue()))
                .toList())
        .execute();
  }
}
