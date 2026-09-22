package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.javamoney.moneta.Money;

public record CreateExpenseDto(
    ExpenseId id,
    Description description,
    GroupId groupId,
    UserId createdBy,
    Instant doneAt,
    Map<UserId, Money> shares,
    Map<UserId, Money> payers) {

  public CreateExpenseDto {
    validateAmounts(shares, payers);
  }

  private static void validateAmounts(
      final Map<UserId, Money> shared, final Map<UserId, Money> payers) {
    if (shared.isEmpty() || payers.isEmpty()) {
      throw new IllegalArgumentException("Shared and payer amounts must not be empty");
    }

    final List<Money> amounts =
        Stream.concat(shared.values().stream(), payers.values().stream()).toList();
    final boolean allAmountsArePositive = amounts.stream().allMatch(Money::isPositive);
    if (!allAmountsArePositive) {
      throw new IllegalArgumentException("All shares and payer amounts must be positive");
    }

    final boolean hasSingleCurrency =
        amounts.stream().map(Money::getCurrency).distinct().count() == 1;
    if (!hasSingleCurrency) {
      throw new IllegalArgumentException("All shares and payer amounts must use the same currency");
    }

    final BigDecimal sharedTotal = total(shared);
    final BigDecimal payerTotal = total(payers);
    if (sharedTotal.compareTo(payerTotal) != 0) {
      throw new IllegalArgumentException("Shared and payer amounts must have the same total");
    }
  }

  private static BigDecimal total(final Map<UserId, Money> amounts) {
    return amounts.values().stream()
        .map(amount -> amount.getNumber().numberValue(BigDecimal.class))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
