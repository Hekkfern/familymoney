package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import lombok.Builder;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdatePaymentDto(
    @Nullable Description description,
    @Nullable Instant doneAt,
    @Nullable Money amount,
    @Nullable UserId creditor,
    @Nullable UserId debitor) {

  public boolean isEmpty() {
    return description == null
        && doneAt == null
        && amount == null
        && creditor == null
        && debitor == null;
  }
}
