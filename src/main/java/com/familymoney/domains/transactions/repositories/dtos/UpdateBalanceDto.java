package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.user.types.UserId;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.Nullable;

public record UpdateBalanceDto(
    @Nullable Money money, @Nullable UserId user1, @Nullable UserId user2) {

  public boolean isEmpty() {
    return money == null && user1 == null && user2 == null;
  }
}
