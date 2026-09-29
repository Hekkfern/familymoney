package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.validations.PositiveMoney;
import com.familymoney.domains.transactions.validations.ValidCurrencyCode;
import com.familymoney.domains.transactions.validations.ValidDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.Instant;
import java.util.UUID;
import org.javamoney.moneta.Money;

public record CreatePaymentRequestDto(
    @NotNull UUID groupId,
    @NotNull @ValidDescription String description,
    @NotNull @PositiveMoney Money amount,
    @NotNull @ValidCurrencyCode String currency,
    @NotNull @Past Instant doneAt,
    @NotNull UUID createdBy,
    @NotNull UUID from,
    @NotNull UUID to) {

  @JsonIgnore
  @AssertTrue(message = "from and to must be different")
  public boolean isFromDifferentFromTo() {
    return from == null || to == null || !from.equals(to);
  }
}
