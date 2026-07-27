package com.familymoney.domains.transactions.services.data;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import lombok.Builder;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateTransactionData(
    @Nullable Description description,
    @Nullable UserId from,
    @Nullable UserId to,
    @Nullable Money amount,
    @Nullable Instant doneAt) {

  public boolean isEmpty() {
    return description == null && from == null && to == null && amount == null && doneAt == null;
  }
}
