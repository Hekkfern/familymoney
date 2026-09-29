package com.familymoney.domains.transactions.controllers.dtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

final class ExpenseRequestChecks {

  private ExpenseRequestChecks() {
    /* This utility class should not be instantiated */
  }

  public static <T> boolean hasUniqueUsers(
      final @Nullable List<T> entries, final Function<T, @Nullable UUID> userIdExtractor) {
    if (entries == null) {
      return true;
    }
    return entries.stream().map(userIdExtractor).distinct().count() == entries.size();
  }

  public static <S, P> boolean isTotalsMatching(
      final @Nullable List<S> shares,
      final Function<S, @Nullable BigDecimal> shareAmountExtractor,
      final @Nullable List<P> payers,
      final Function<P, @Nullable BigDecimal> payerAmountExtractor) {
    if (shares == null || payers == null) {
      return true;
    }
    final List<@Nullable BigDecimal> shareAmounts =
        shares.stream().map(shareAmountExtractor).toList();
    final List<@Nullable BigDecimal> payerAmounts =
        payers.stream().map(payerAmountExtractor).toList();
    final boolean hasMissingAmount =
        shareAmounts.stream().anyMatch(Objects::isNull)
            || payerAmounts.stream().anyMatch(Objects::isNull);
    if (hasMissingAmount) {
      return true;
    }
    return total(shareAmounts).compareTo(total(payerAmounts)) == 0;
  }

  private static BigDecimal total(final List<@Nullable BigDecimal> amounts) {
    return amounts.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
