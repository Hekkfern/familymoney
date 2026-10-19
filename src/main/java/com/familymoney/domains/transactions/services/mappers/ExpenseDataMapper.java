package com.familymoney.domains.transactions.services.mappers;

import com.familymoney.domains.transactions.repositories.entitites.ExpenseEntity;
import com.familymoney.domains.transactions.repositories.entitites.ExpensePaymentEntity;
import com.familymoney.domains.transactions.repositories.entitites.ExpenseShareEntity;
import com.familymoney.domains.transactions.repositories.entitites.FullExpenseEntity;
import com.familymoney.domains.transactions.services.data.ExpenseData;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.stream.Collectors;
import org.javamoney.moneta.Money;

public final class ExpenseDataMapper {

  private ExpenseDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static ExpenseData fromDbo(final FullExpenseEntity entity, final Currency currency) {
    final ExpenseEntity expense = entity.expense();
    final Map<UserId, BigDecimal> shares =
        entity.shares().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    ExpenseShareEntity::userId, ExpenseShareEntity::amount));
    final Map<UserId, BigDecimal> payers =
        entity.payments().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    ExpensePaymentEntity::userId, ExpensePaymentEntity::amount));
    final BigDecimal totalAmount =
        shares.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    return new ExpenseData(
        expense.id(),
        expense.groupId(),
        expense.description(),
        Money.of(totalAmount, currency.getCurrencyCode()),
        expense.doneAt(),
        shares,
        payers);
  }
}
