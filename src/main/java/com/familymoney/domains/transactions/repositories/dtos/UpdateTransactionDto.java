package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import lombok.Builder;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateTransactionDto(
    @Nullable Money amount,
    @Nullable Description description,
    @Nullable UserId from,
    @Nullable UserId to,
    @Nullable Instant doneAt) {

  public boolean isEmpty() {
    return amount == null && description == null && from == null && to == null && doneAt == null;
  }
}
