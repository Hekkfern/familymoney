package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.validations.PositiveMoney;
import com.familymoney.domains.transactions.validations.ValidDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreatePaymentRequestDto(
    @NotNull UUID groupId,
    @NotNull @ValidDescription String description,
    @NotNull @PositiveMoney BigDecimal amount,
    @NotNull @Past Instant doneAt,
    @NotNull UUID from,
    @NotNull UUID to) {

  @JsonIgnore
  @AssertTrue(message = "from and to must be different")
  public boolean isFromDifferentFromTo() {
    return !from.equals(to);
  }
}
