package com.familymoney.controllers.dtos.grouptransaction;

import com.familymoney.validation.DifferentFromTo;
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
