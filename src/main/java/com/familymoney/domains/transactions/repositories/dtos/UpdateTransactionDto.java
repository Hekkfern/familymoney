package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.user.types.UserId;
import java.time.Instant;
import lombok.Builder;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateTransactionDto(
    @Nullable Money amount,
    @Nullable String description,
    @Nullable UserId from,
    @Nullable UserId to,
    @Nullable Instant doneAt) {

  public boolean isEmpty() {
    return amount == null && description == null && from == null && to == null && doneAt == null;
  }
}
