package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.validations.PositiveMoney;
import com.familymoney.domains.transactions.validations.ValidDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Past;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdatePaymentRequestDto(
    @Nullable @ValidDescription String description,
    @Nullable UUID from,
    @Nullable UUID to,
    @Nullable @PositiveMoney Money amount,
    @Nullable @Past Instant doneAt) {

  @JsonIgnore
  @AssertTrue(message = "At least one field must be provided")
  public boolean isAnyFieldProvided() {
    return description != null || from != null || to != null || amount != null || doneAt != null;
  }

  @JsonIgnore
  @AssertTrue(message = "from and to must be different")
  public boolean isFromDifferentFromTo() {
    return from == null || to == null || !from.equals(to);
  }
}
