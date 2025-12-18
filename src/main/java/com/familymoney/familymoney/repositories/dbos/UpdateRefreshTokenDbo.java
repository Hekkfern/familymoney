package com.familymoney.familymoney.repositories.dbos;

import java.time.Instant;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@Builder
public class UpdateRefreshTokenDbo {
  @Nullable @Default private Boolean isUsed = null;
  @Nullable @Default private Instant usedAt = null;

  public boolean isEmpty() {
    return isUsed == null && usedAt == null;
  }
}
