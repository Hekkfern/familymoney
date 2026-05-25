package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.user.types.UserId;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.Nullable;

@Data
@Builder
public class UpdateBalanceDto {
  @Nullable @Default private Money money = null;
  @Nullable @Default private UserId user1 = null;
  @Nullable @Default private UserId user2 = null;

  public boolean isEmpty() {
    return money == null && user1 == null && user2 == null;
  }
}
