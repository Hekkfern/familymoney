package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.Nullable;

@Data
@Builder
public class UpdateTransactionDbo {
  @Nullable @Default private Money amount = null;
  @Nullable @Default private String description = null;
  @Nullable @Default private UserId from = null;
  @Nullable @Default private UserId to = null;
  @Nullable @Default private Instant doneAt = null;

  public boolean isEmpty() {
    return amount == null && description == null && from == null && to == null && doneAt == null;
  }
}
