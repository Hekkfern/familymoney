package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.validations.DifferentFromTo;
import com.familymoney.domains.transactions.validations.PositiveMoney;
import com.familymoney.domains.transactions.validations.ValidCurrencyCode;
import com.familymoney.domains.transactions.validations.ValidDescription;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.javamoney.moneta.Money;

@DifferentFromTo
public record CreateExpenseRequestDto(
    @NotNull @ValidDescription String description,
    @NotNull UUID groupId,
    @NotNull @ValidCurrencyCode String currency,
    @NotNull @Past Instant doneAt,
    @NotNull UUID createdBy,
    @NotEmpty List<ExpenseShareDto> shares,
    @NotEmpty List<ExpensePayerDto> payers) {

  public record ExpenseShareDto(@NotNull UUID userId, @NotNull @PositiveMoney Money amount) {}

  public record ExpensePayerDto(@NotNull UUID userId, @NotNull @PositiveMoney Money amount) {}
}
