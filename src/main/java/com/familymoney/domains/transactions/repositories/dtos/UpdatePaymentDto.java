package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdatePaymentDto(
    @Nullable Description description,
    @Nullable Instant doneAt,
    @Nullable BigDecimal amount,
    @Nullable UserId creditor,
    @Nullable UserId debitor) {

  private static final int MAX_AMOUNT_SCALE = 3;

  public UpdatePaymentDto {
    if (isEmpty()) {
      throw new IllegalArgumentException("At least one field must be provided for update");
    }
    final boolean isSameUser = creditor != null && creditor.equals(debitor);
    if (isSameUser) {
      throw new IllegalArgumentException("Creditor and debitor must be different users");
    }
    final boolean isInvalidAmount =
        amount != null && (amount.signum() <= 0 || amount.scale() > MAX_AMOUNT_SCALE);
    if (isInvalidAmount) {
      throw new IllegalArgumentException("Invalid payment amount");
    }
  }

  public boolean isEmpty() {
    return description == null
        && doneAt == null
        && amount == null
        && creditor == null
        && debitor == null;
  }
}
