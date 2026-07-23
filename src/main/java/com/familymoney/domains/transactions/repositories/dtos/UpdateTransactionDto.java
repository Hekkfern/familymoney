package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.user.types.UserId;
import java.time.Instant;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.Nullable;

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
