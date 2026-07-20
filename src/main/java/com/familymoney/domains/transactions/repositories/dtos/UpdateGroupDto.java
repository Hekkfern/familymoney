package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.transactions.types.GroupName;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateGroupDto(@Nullable GroupName name, @Nullable String description) {

  public boolean isEmpty() {
    return name == null && description == null;
  }
}
