package com.familymoney.familymoney.controllers.dtos.grouptransaction;

import com.familymoney.familymoney.validation.DifferentFromTo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.Instant;
import java.util.UUID;
import org.javamoney.moneta.Money;

@DifferentFromTo
public record CreateTransactionRequestDto(
    @NotNull String description,
    @NotNull UUID from,
    @NotNull UUID to,
    @NotNull Money amount,
    @NotNull @Past Instant doneAt) {}
