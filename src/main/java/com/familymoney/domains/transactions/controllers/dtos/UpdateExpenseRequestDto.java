package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.validations.PositiveMoney;
import com.familymoney.domains.transactions.validations.ValidDescription;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateExpenseRequestDto(
    @Nullable @ValidDescription String description,
    @Nullable @Past Instant doneAt,
    @Nullable UUID createdBy,
    @Nullable List<ExpenseShareDto> shares,
    @Nullable List<ExpensePayerDto> payers) {

  public record ExpenseShareDto(@NotNull UUID userId, @NotNull @PositiveMoney Money amount) {}

  public record ExpensePayerDto(@NotNull UUID userId, @NotNull @PositiveMoney Money amount) {}
}
