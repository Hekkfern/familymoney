package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.validations.DifferentFromTo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.Instant;
import java.util.UUID;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.Nullable;

@DifferentFromTo
public record UpdateTransactionRequestDto(
    @NotNull UUID id,
    @Nullable String description,
    @Nullable UUID from,
    @Nullable UUID to,
    @Nullable Money amount,
    @Nullable @Past Instant doneAt) {}
