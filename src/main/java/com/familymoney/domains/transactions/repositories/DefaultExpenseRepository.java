package com.familymoney.domains.transactions.repositories;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;

import com.familymoney.domains.transactions.repositories.dtos.CreateExpenseDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateExpenseDto;
import com.familymoney.domains.transactions.repositories.entitites.FullExpenseEntity;
import com.familymoney.domains.transactions.repositories.exceptions.CreateExpenseException;
import com.familymoney.domains.transactions.repositories.exceptions.DeleteExpenseException;
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
import com.familymoney.generated.tables.Groups;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class DefaultExpenseRepository implements ExpenseRepository {

  private final DSLContext db;

  @Transactional
  @Override
  public void create(final CreateExpenseDto dto) {
    final String groupCurrencyCode = fetchGroupCurrencyCode(dto.groupId());
    final String expenseCurrencyCode =
        dto.payers().values().stream().findFirst().orElseThrow().getCurrency().getCurrencyCode();
    if (!groupCurrencyCode.equals(expenseCurrencyCode)) {
      throw new CreateExpenseException(
          "Expense in Group '%s' must use currency: %s"
              .formatted(dto.groupId().value(), groupCurrencyCode));
    }

    final int expensesCreated =
        db.insertInto(Expenses.EXPENSES)
            .columns(
                Expenses.EXPENSES.ID,
                Expenses.EXPENSES.DESCRIPTION,
                Expenses.EXPENSES.GROUP_ID,
                Expenses.EXPENSES.CURRENCY_CODE,
                Expenses.EXPENSES.DONE_AT,
                Expenses.EXPENSES.CREATED_BY)
            .values(
                dto.id().value(),
                dto.description().value(),
                dto.groupId().value(),
                expenseCurrencyCode,
                OffsetDateTime.ofInstant(dto.doneAt(), DEFAULT_TIMEZONE_OFFSET),
                dto.createdBy().value())
            .execute();
    if (expensesCreated != 1) {
      throw new CreateExpenseException(
          "Could not create expense with ID: %s".formatted(dto.id().value()));
    }
    final int createdShares =
        db.insertInto(
                ExpenseShares.EXPENSE_SHARES,
                ExpenseShares.EXPENSE_SHARES.EXPENSE_ID,
                ExpenseShares.EXPENSE_SHARES.USER_ID,
                ExpenseShares.EXPENSE_SHARES.AMOUNT)
            .valuesOfRows(
                dto.shares().entrySet().stream()
                    .map(
                        shareEntry ->
                            DSL.row(
                                dto.id().value(),
                                shareEntry.getKey().value(),
                                shareEntry.getValue().getNumber().numberValue(BigDecimal.class)))
                    .toList())
            .execute();
    if (createdShares != dto.shares().size()) {
      throw new CreateExpenseException(
          "Could not create expense shares for expense ID: %s".formatted(dto.id().value()));
    }
    final int createdPayments =
        db.insertInto(
                ExpensePayments.EXPENSE_PAYMENTS,
                ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID,
                ExpensePayments.EXPENSE_PAYMENTS.USER_ID,
                ExpensePayments.EXPENSE_PAYMENTS.AMOUNT)
            .valuesOfRows(
                dto.payers().entrySet().stream()
                    .map(
                        payerEntry ->
                            DSL.row(
                                dto.id().value(),
                                payerEntry.getKey().value(),
                                payerEntry.getValue().getNumber().numberValue(BigDecimal.class)))
                    .toList())
            .execute();
    if (createdPayments != dto.payers().size()) {
      throw new CreateExpenseException(
          "Could not create expense payments for expense ID: %s".formatted(dto.id().value()));
    }
  }

  @Transactional
  @Override
  public void updateById(final ExpenseId id, final UpdateExpenseDto dto) {
    if (dto.isEmpty()) {
      return;
    }
    final String currencyCode = lockAndGetCurrencyCode(id);
    validateAmounts(dto.shares(), currencyCode, "shares");
    validateAmounts(dto.payers(), currencyCode, "payments");

    if (dto.shares() != null || dto.payers() != null) {
      final BigDecimal sharesTotal =
          dto.shares() != null ? total(dto.shares()) : findSharesTotal(id);
      final BigDecimal paymentsTotal =
          dto.payers() != null ? total(dto.payers()) : findPaymentsTotal(id);
      if (sharesTotal.compareTo(paymentsTotal) != 0) {
        throw new UpdateExpenseException(
            "Expense shares and payments must have the same total for ID: %s"
                .formatted(id.value()));
      }
    }

    final boolean expenseFieldsChanged = updateExpenseFields(id, dto);
    final boolean sharesChanged = dto.shares() != null && synchronizeShares(id, dto.shares());
    final boolean paymentsChanged = dto.payers() != null && synchronizePayments(id, dto.payers());
    if (!expenseFieldsChanged && (sharesChanged || paymentsChanged)) {
      touchUpdatedAt(id);
    }
  }

  private String fetchGroupCurrencyCode(final GroupId groupId) {
    return db.select(Groups.GROUPS.CURRENCY_CODE)
        .from(Groups.GROUPS)
        .where(Groups.GROUPS.ID.eq(groupId.value()))
        .fetchOptional(Groups.GROUPS.CURRENCY_CODE)
        .orElseThrow(
            () ->
                new CreateExpenseException(
                    "Could not find group with ID: %s".formatted(groupId.value())));
  }

  private String lockAndGetCurrencyCode(final ExpenseId id) {
    return db.select(Expenses.EXPENSES.CURRENCY_CODE)
        .from(Expenses.EXPENSES)
        .where(Expenses.EXPENSES.ID.eq(id.value()))
        .forUpdate()
        .fetchOptional(Expenses.EXPENSES.CURRENCY_CODE)
        .orElseThrow(
            () ->
                new UpdateExpenseException(
                    "Could not find expense with ID: %s".formatted(id.value())));
  }

  private static void validateAmounts(
      final @Nullable Map<UserId, Money> amounts, final String currencyCode, final String name) {
    if (amounts == null) {
      return;
    }
    if (amounts.isEmpty()) {
      throw new UpdateExpenseException("Expense %s must not be empty".formatted(name));
    }
    final boolean positiveAmounts = amounts.values().stream().allMatch(Money::isPositive);
    if (!positiveAmounts) {
      throw new UpdateExpenseException("Expense %s must be positive".formatted(name));
    }
    final boolean matchingCurrency =
        amounts.values().stream()
            .allMatch(amount -> currencyCode.equals(amount.getCurrency().getCurrencyCode()));
    if (!matchingCurrency) {
      throw new UpdateExpenseException(
          "Expense %s must use currency: %s".formatted(name, currencyCode));
    }
  }

  private boolean updateExpenseFields(final ExpenseId id, final UpdateExpenseDto dto) {
    if (dto.description() == null && dto.doneAt() == null) {
      return false;
    }
    final int updatedRows;
    if (dto.description() != null && dto.doneAt() != null) {
      updatedRows =
          db.update(Expenses.EXPENSES)
              .set(Expenses.EXPENSES.DESCRIPTION, dto.description().value())
              .set(
                  Expenses.EXPENSES.DONE_AT,
                  OffsetDateTime.ofInstant(dto.doneAt(), DEFAULT_TIMEZONE_OFFSET))
              .where(Expenses.EXPENSES.ID.eq(id.value()))
              .execute();
    } else if (dto.description() != null) {
      updatedRows =
          db.update(Expenses.EXPENSES)
              .set(Expenses.EXPENSES.DESCRIPTION, dto.description().value())
              .where(Expenses.EXPENSES.ID.eq(id.value()))
              .execute();
    } else {
      updatedRows =
          db.update(Expenses.EXPENSES)
              .set(
                  Expenses.EXPENSES.DONE_AT,
                  OffsetDateTime.ofInstant(dto.doneAt(), DEFAULT_TIMEZONE_OFFSET))
              .where(Expenses.EXPENSES.ID.eq(id.value()))
              .execute();
    }
    if (updatedRows != 1) {
      throw new UpdateExpenseException("Could not update expense ID: %s".formatted(id.value()));
    }
    return true;
  }

  private boolean synchronizeShares(final ExpenseId id, final Map<UserId, Money> shares) {
    final int insertedOrUpdatedRows =
        db.insertInto(
                ExpenseShares.EXPENSE_SHARES,
                ExpenseShares.EXPENSE_SHARES.EXPENSE_ID,
                ExpenseShares.EXPENSE_SHARES.USER_ID,
                ExpenseShares.EXPENSE_SHARES.AMOUNT)
            .valuesOfRows(
                shares.entrySet().stream()
                    .map(
                        entry ->
                            DSL.row(
                                id.value(),
                                entry.getKey().value(),
                                entry.getValue().getNumber().numberValue(BigDecimal.class)))
                    .toList())
            .onConflict(
                ExpenseShares.EXPENSE_SHARES.EXPENSE_ID, ExpenseShares.EXPENSE_SHARES.USER_ID)
            .doUpdate()
            .set(
                ExpenseShares.EXPENSE_SHARES.AMOUNT,
                DSL.excluded(ExpenseShares.EXPENSE_SHARES.AMOUNT))
            .where(
                ExpenseShares.EXPENSE_SHARES.AMOUNT.isDistinctFrom(
                    DSL.excluded(ExpenseShares.EXPENSE_SHARES.AMOUNT)))
            .execute();
    final int deletedRows =
        db.deleteFrom(ExpenseShares.EXPENSE_SHARES)
            .where(ExpenseShares.EXPENSE_SHARES.EXPENSE_ID.eq(id.value()))
            .and(
                ExpenseShares.EXPENSE_SHARES.USER_ID.notIn(
                    shares.keySet().stream().map(UserId::value).toList()))
            .execute();
    return insertedOrUpdatedRows > 0 || deletedRows > 0;
  }

  private boolean synchronizePayments(final ExpenseId id, final Map<UserId, Money> payments) {
    final int insertedOrUpdatedRows =
        db.insertInto(
                ExpensePayments.EXPENSE_PAYMENTS,
                ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID,
                ExpensePayments.EXPENSE_PAYMENTS.USER_ID,
                ExpensePayments.EXPENSE_PAYMENTS.AMOUNT)
            .valuesOfRows(
                payments.entrySet().stream()
                    .map(
                        entry ->
                            DSL.row(
                                id.value(),
                                entry.getKey().value(),
                                entry.getValue().getNumber().numberValue(BigDecimal.class)))
                    .toList())
            .onConflict(
                ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID,
                ExpensePayments.EXPENSE_PAYMENTS.USER_ID)
            .doUpdate()
            .set(
                ExpensePayments.EXPENSE_PAYMENTS.AMOUNT,
                DSL.excluded(ExpensePayments.EXPENSE_PAYMENTS.AMOUNT))
            .where(
                ExpensePayments.EXPENSE_PAYMENTS.AMOUNT.isDistinctFrom(
                    DSL.excluded(ExpensePayments.EXPENSE_PAYMENTS.AMOUNT)))
            .execute();
    final int deletedRows =
        db.deleteFrom(ExpensePayments.EXPENSE_PAYMENTS)
            .where(ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID.eq(id.value()))
            .and(
                ExpensePayments.EXPENSE_PAYMENTS.USER_ID.notIn(
                    payments.keySet().stream().map(UserId::value).toList()))
            .execute();
    return insertedOrUpdatedRows > 0 || deletedRows > 0;
  }

  private void touchUpdatedAt(final ExpenseId id) {
    db.update(Expenses.EXPENSES)
        .set(Expenses.EXPENSES.UPDATED_AT, DSL.currentOffsetDateTime())
        .where(Expenses.EXPENSES.ID.eq(id.value()))
        .execute();
  }

  private BigDecimal findSharesTotal(final ExpenseId id) {
    final BigDecimal total =
        db.select(DSL.sum(ExpenseShares.EXPENSE_SHARES.AMOUNT))
            .from(ExpenseShares.EXPENSE_SHARES)
            .where(ExpenseShares.EXPENSE_SHARES.EXPENSE_ID.eq(id.value()))
            .fetchOne(0, BigDecimal.class);
    return total != null ? total : BigDecimal.ZERO;
  }

  private BigDecimal findPaymentsTotal(final ExpenseId id) {
    final BigDecimal total =
        db.select(DSL.sum(ExpensePayments.EXPENSE_PAYMENTS.AMOUNT))
            .from(ExpensePayments.EXPENSE_PAYMENTS)
            .where(ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID.eq(id.value()))
            .fetchOne(0, BigDecimal.class);
    return total != null ? total : BigDecimal.ZERO;
  }

  private static BigDecimal total(final Map<UserId, Money> amounts) {
    return amounts.values().stream()
        .map(amount -> amount.getNumber().numberValue(BigDecimal.class))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  public void deleteById(final ExpenseId id) {
    final int rowsAffected =
        db.deleteFrom(Expenses.EXPENSES).where(Expenses.EXPENSES.ID.eq(id.value())).execute();
    if (rowsAffected == 0) {
      throw new DeleteExpenseException("Could not delete expense ID: %s".formatted(id.value()));
    }
  }

  @Override
  public Optional<FullExpenseEntity> findById(final ExpenseId id) {
    return db.select(
            Expenses.EXPENSES.ID,
            Expenses.EXPENSES.DESCRIPTION,
            Expenses.EXPENSES.GROUP_ID,
            Expenses.EXPENSES.CURRENCY_CODE,
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
                    record.value6().map(ExpenseShareJooqMapper::toEntity),
                    record.value7().map(ExpensePaymentJooqMapper::toEntity)));
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
                Expenses.EXPENSES.CURRENCY_CODE,
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
                        record.value6().map(ExpenseShareJooqMapper::toEntity),
                        record.value7().map(ExpensePaymentJooqMapper::toEntity)));
    return new PageImpl<>(data, pageable, safeTotal);
  }
}
