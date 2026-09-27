package com.familymoney.domains.transactions.repositories;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;

import com.familymoney.domains.transactions.repositories.dtos.CreateExpenseDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateExpenseDto;
import com.familymoney.domains.transactions.repositories.entitites.FullExpenseEntity;
import com.familymoney.domains.transactions.repositories.exceptions.CreateExpenseException;
import com.familymoney.domains.transactions.repositories.exceptions.UpdateExpenseException;
import com.familymoney.domains.transactions.repositories.mappers.ExpenseJooqMapper;
import com.familymoney.domains.transactions.repositories.mappers.ExpensePaymentJooqMapper;
import com.familymoney.domains.transactions.repositories.mappers.ExpenseShareJooqMapper;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.ExpensePayments;
import com.familymoney.generated.tables.ExpenseShares;
import com.familymoney.generated.tables.Expenses;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
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
    final int expensesCreated =
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
    if (expensesCreated != 1) {
      final String msg = "Could not create expense with ID: %s".formatted(dto.id().value());
      log.error(msg);
      throw new CreateExpenseException(msg);
    }
    final int sharesCreated = insertShares(dto.id(), dto.shares());
    if (sharesCreated != dto.shares().size()) {
      final String msg =
          "Could not create expense shares for expense ID: %s".formatted(dto.id().value());
      log.error(msg);
      throw new CreateExpenseException(msg);
    }
    final int paymentsCreated = insertPayments(dto.id(), dto.payers());
    if (paymentsCreated != dto.payers().size()) {
      final String msg =
          "Could not create expense payments for expense ID: %s".formatted(dto.id().value());
      log.error(msg);
      throw new CreateExpenseException(msg);
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
    if (expensesUpdated != 1) {
      final String msg = "Could not update expense ID: %s".formatted(id.value());
      log.error(msg);
      throw new UpdateExpenseException(msg);
    }

    final Map<UserId, BigDecimal> shares = dto.shares();
    if (shares != null) {
      db.deleteFrom(ExpenseShares.EXPENSE_SHARES)
          .where(ExpenseShares.EXPENSE_SHARES.EXPENSE_ID.eq(id.value()))
          .execute();
      final int sharesCreated = insertShares(id, shares);
      if (sharesCreated != shares.size()) {
        final String msg =
            "Could not update expense shares for expense ID: %s".formatted(id.value());
        log.error(msg);
        throw new UpdateExpenseException(msg);
      }
    }

    final Map<UserId, BigDecimal> payers = dto.payers();
    if (payers != null) {
      db.deleteFrom(ExpensePayments.EXPENSE_PAYMENTS)
          .where(ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID.eq(id.value()))
          .execute();
      final int paymentsCreated = insertPayments(id, payers);
      if (paymentsCreated != payers.size()) {
        final String msg =
            "Could not update expense payments for expense ID: %s".formatted(id.value());
        log.error(msg);
        throw new UpdateExpenseException(msg);
      }
    }
  }

  @Override
  public void deleteById(final ExpenseId id) {
    final int rowsAffected =
        db.deleteFrom(Expenses.EXPENSES).where(Expenses.EXPENSES.ID.eq(id.value())).execute();
    if (rowsAffected != 1) {
      log.warn("Could not delete expense ID: {}", id.value());
    }
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
            .orderBy(Expenses.EXPENSES.DONE_AT.desc())
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

  private int insertShares(final ExpenseId expenseId, final Map<UserId, BigDecimal> shares) {
    return db.insertInto(
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

  private int insertPayments(final ExpenseId expenseId, final Map<UserId, BigDecimal> payers) {
    return db.insertInto(
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
