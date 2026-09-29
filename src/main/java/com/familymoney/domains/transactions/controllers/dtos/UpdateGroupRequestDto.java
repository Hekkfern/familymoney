package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.validations.ValidDescription;
import com.familymoney.domains.transactions.validations.ValidGroupName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import org.jspecify.annotations.Nullable;

public record UpdateGroupRequestDto(
    @Nullable @ValidGroupName String name, @Nullable @ValidDescription String description) {

  @JsonIgnore
  @AssertTrue(message = "At least one field must be provided")
  public boolean isAnyFieldProvided() {
    return name != null || description != null;
  }
}
