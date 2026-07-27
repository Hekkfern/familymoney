package com.familymoney.domains.transactions.services.data;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupName;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateGroupData(@Nullable GroupName name, @Nullable Description description) {

  public boolean isEmpty() {
    return name == null && description == null;
  }
}
