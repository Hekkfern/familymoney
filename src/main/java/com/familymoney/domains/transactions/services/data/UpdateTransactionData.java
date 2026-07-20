package com.familymoney.domains.transactions.services.data;

import com.familymoney.domains.user.types.UserId;
import java.time.Instant;
import lombok.Builder;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateTransactionData(
    @Nullable String description,
    @Nullable UserId from,
    @Nullable UserId to,
    @Nullable Money amount,
    @Nullable Instant doneAt) {

  public boolean isEmpty() {
    return description == null && from == null && to == null && amount == null && doneAt == null;
  }
}
