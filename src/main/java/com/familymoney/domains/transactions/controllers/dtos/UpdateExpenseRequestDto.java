package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.validations.ValidDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateExpenseRequestDto(
    @Nullable @ValidDescription String description,
    @Nullable @Past Instant doneAt,
    @Nullable UUID createdBy,
    @Nullable @NotEmpty @Valid List<ExpenseShareDto> shares,
    @Nullable @NotEmpty @Valid List<ExpensePayerDto> payers) {

  public record ExpenseShareDto(@NotNull UUID userId, @NotNull @Positive BigDecimal amount) {}

  public record ExpensePayerDto(@NotNull UUID userId, @NotNull @Positive BigDecimal amount) {}

  @JsonIgnore
  @AssertTrue(message = "At least one field must be provided")
  public boolean isAnyFieldProvided() {
    return description != null
        || doneAt != null
        || createdBy != null
        || shares != null
        || payers != null;
  }

  @JsonIgnore
  @AssertTrue(message = "Shares and payers must be provided together")
  public boolean isSharesAndPayersProvidedTogether() {
    return (shares == null) == (payers == null);
  }

  @JsonIgnore
  @AssertTrue(message = "Each user can appear only once in the shares")
  public boolean isShareUsersUnique() {
    return ExpenseRequestChecks.hasUniqueUsers(shares, ExpenseShareDto::userId);
  }

  @JsonIgnore
  @AssertTrue(message = "Each user can appear only once in the payers")
  public boolean isPayerUsersUnique() {
    return ExpenseRequestChecks.hasUniqueUsers(payers, ExpensePayerDto::userId);
  }

  @JsonIgnore
  @AssertTrue(message = "Shares and payers totals must match")
  public boolean isTotalsMatching() {
    return ExpenseRequestChecks.isTotalsMatching(
        shares, ExpenseShareDto::amount, payers, ExpensePayerDto::amount);
  }
}
