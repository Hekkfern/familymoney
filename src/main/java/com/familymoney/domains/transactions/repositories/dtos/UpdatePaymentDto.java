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

  public UpdatePaymentDto {
    if (isEmpty()) {
      throw new IllegalArgumentException("At least one field must be provided for update");
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
