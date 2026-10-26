package com.familymoney.domains.transactions.repositories.dbos;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupName;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateGroupDbo(@Nullable GroupName name, @Nullable Description description) {

  public UpdateGroupDbo {
    final boolean isEmptyUpdate = name == null && description == null;
    if (isEmptyUpdate) {
      throw new IllegalArgumentException("At least one field must be provided for update");
    }
  }

  public boolean isEmpty() {
    return name == null && description == null;
  }
}
