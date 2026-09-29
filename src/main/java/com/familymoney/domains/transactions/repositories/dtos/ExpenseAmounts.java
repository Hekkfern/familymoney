package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Stream;

final class ExpenseAmounts {

  private static final int MAX_AMOUNT_SCALE = 3;

  private ExpenseAmounts() {
    /* This utility class should not be instantiated */
  }

  static void validate(final Map<UserId, BigDecimal> shares, final Map<UserId, BigDecimal> payers) {
    if (shares.isEmpty() || payers.isEmpty()) {
      throw new IllegalArgumentException("Shares and payers must not be empty");
    }
    final boolean hasInvalidAmount =
        Stream.concat(shares.values().stream(), payers.values().stream())
            .anyMatch(ExpenseAmounts::isInvalidAmount);
    if (hasInvalidAmount) {
      throw new IllegalArgumentException("Invalid expense amount");
    }
    if (total(shares).compareTo(total(payers)) != 0) {
      throw new IllegalArgumentException("Shares and payers totals must match");
    }
  }

  private static boolean isInvalidAmount(final BigDecimal amount) {
    return amount.signum() <= 0 || amount.scale() > MAX_AMOUNT_SCALE;
  }

  private static BigDecimal total(final Map<UserId, BigDecimal> amounts) {
    return amounts.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
