package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateExpenseDto(
    @Nullable Description description,
    @Nullable Instant doneAt,
    @Nullable Map<UserId, BigDecimal> shares,
    @Nullable Map<UserId, BigDecimal> payers) {

  public UpdateExpenseDto {
    if (isEmpty()) {
      throw new IllegalArgumentException("At least one field must be provided for update");
    }
    if ((shares == null) != (payers == null)) {
      throw new IllegalArgumentException("Shares and payers must be provided together");
    }
    if (shares != null && payers != null) {
      ExpenseAmounts.validate(shares, payers);
      shares = Map.copyOf(shares);
      payers = Map.copyOf(payers);
    }
  }

  public boolean isEmpty() {
    return description == null && doneAt == null && shares == null && payers == null;
  }
}
