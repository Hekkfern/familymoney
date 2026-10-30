package com.familymoney.domains.transactions.services.data;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdatePaymentData(
    @Nullable Description description,
    @Nullable Instant doneAt,
    @Nullable BigDecimal amount,
    @Nullable UserId from,
    @Nullable UserId to) {

  public UpdatePaymentData {
    final boolean isEmptyUpdate =
        description == null && doneAt == null && amount == null && from == null && to == null;
    if (isEmptyUpdate) {
      throw new IllegalArgumentException("At least one field must be provided for update");
    }
  }

  public boolean isEmpty() {
    return description == null && doneAt == null && amount == null && from == null && to == null;
  }
}
