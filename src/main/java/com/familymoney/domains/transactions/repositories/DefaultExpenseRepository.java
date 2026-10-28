package com.familymoney.domains.transactions.repositories;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;

import com.familymoney.domains.transactions.repositories.dbos.CreateExpenseDbo;
import com.familymoney.domains.transactions.repositories.dbos.UpdateExpenseDbo;
import com.familymoney.domains.transactions.repositories.entitites.FullExpenseEntity;
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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class DefaultExpenseRepository implements ExpenseRepository {

  private final DSLContext db;

  @Transactional
  @Override
  public void create(final CreateExpenseDbo dto) {
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
  }

  @Transactional
  @Override
  public void updateById(final ExpenseId id, final UpdateExpenseDbo dto) {
    final Map<Field<?>, Object> values = new LinkedHashMap<>();
    if (dto.description() != null) {
      values.put(Expenses.EXPENSES.DESCRIPTION, dto.description().value());
    }
    if (dto.doneAt() != null) {
      values.put(
          Expenses.EXPENSES.DONE_AT,
          OffsetDateTime.ofInstant(dto.doneAt(), DEFAULT_TIMEZONE_OFFSET));
    }
    db.update(Expenses.EXPENSES).set(values).where(Expenses.EXPENSES.ID.eq(id.value())).execute();

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
    db.deleteFrom(ExpenseShares.EXPENSE_SHARES)
        .where(ExpenseShares.EXPENSE_SHARES.EXPENSE_ID.eq(id.value()))
        .execute();
    insertShares(id, shares);
    db.deleteFrom(ExpensePayments.EXPENSE_PAYMENTS)
        .where(ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID.eq(id.value()))
        .execute();
    insertPayments(id, payers);
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
    db.deleteFrom(Expenses.EXPENSES).where(Expenses.EXPENSES.ID.eq(id.value())).execute();
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
    return findAllByCondition(Expenses.EXPENSES.ID.eq(id.value())).stream().findFirst();
  }

  @Override
  public List<FullExpenseEntity> findAllByGroupIdAndIds(
      final GroupId groupId, final Set<ExpenseId> ids) {
    if (ids.isEmpty()) {
      return List.of();
    }
    return findAllByCondition(
        Expenses.EXPENSES
            .GROUP_ID
            .eq(groupId.value())
            .and(Expenses.EXPENSES.ID.in(ids.stream().map(ExpenseId::value).toList())));
  }

  private List<FullExpenseEntity> findAllByCondition(final Condition condition) {
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
                        .where(ExpenseShares.EXPENSE_SHARES.EXPENSE_ID.eq(Expenses.EXPENSES.ID)))
                .as("shares"),
            DSL.multiset(
                    db.select(
                            ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID,
                            ExpensePayments.EXPENSE_PAYMENTS.USER_ID,
                            ExpensePayments.EXPENSE_PAYMENTS.AMOUNT)
                        .from(ExpensePayments.EXPENSE_PAYMENTS)
                        .where(
                            ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID.eq(Expenses.EXPENSES.ID)))
                .as("payments"))
        .from(Expenses.EXPENSES)
        .where(condition)
        .fetch(
            r ->
                new FullExpenseEntity(
                    ExpenseJooqMapper.toEntity(r),
                    r.value5().map(ExpenseShareJooqMapper::toEntity),
                    r.value6().map(ExpensePaymentJooqMapper::toEntity)));
  }

  @Transactional(readOnly = true)
  @Override
  public Page<FullExpenseEntity> findAllByGroupId(final GroupId groupId, final Pageable pageable) {
    final Long total =
        db.selectCount()
            .from(Expenses.EXPENSES)
            .where(Expenses.EXPENSES.GROUP_ID.eq(groupId.value()))
            .fetchOne(0, Long.class);
    final long safeTotal = total != null ? total : 0L;

    final List<SortField<?>> orderFields =
        pageable.getSort().stream()
            .map(
                order -> {
                  final Field<?> field = Expenses.EXPENSES.field(order.getProperty());
                  if (field == null) {
                    throw new IllegalArgumentException(
                        "Unknown sort field: " + order.getProperty());
                  }
                  return order.isAscending() ? field.asc() : field.desc();
                })
            .toList();

    final List<SortField<?>> effectiveOrder =
        orderFields.isEmpty()
            ? List.of(Expenses.EXPENSES.DONE_AT.desc(), Expenses.EXPENSES.ID.desc())
            : orderFields;

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
            .orderBy(effectiveOrder)
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetch(
                r ->
                    new FullExpenseEntity(
                        ExpenseJooqMapper.toEntity(r),
                        r.value5().map(ExpenseShareJooqMapper::toEntity),
                        r.value6().map(ExpensePaymentJooqMapper::toEntity)));
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
