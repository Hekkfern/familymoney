package com.familymoney.domains.users.controllers.dtos;

import com.familymoney.domains.users.validations.ValidEmail;
import com.familymoney.domains.users.validations.ValidPassword;
import com.familymoney.domains.users.validations.ValidUserName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateUserRequestDto(
    @Nullable @ValidUserName String username,
    @Nullable @ValidEmail String email,
    @Nullable @ValidPassword String password) {

  @JsonIgnore
  @AssertTrue(message = "At least one field must be provided")
  public boolean isAnyFieldProvided() {
    return username != null || email != null || password != null;
  }
}
