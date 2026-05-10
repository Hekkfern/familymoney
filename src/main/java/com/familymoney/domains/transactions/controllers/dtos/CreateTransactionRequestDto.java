package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.validations.DifferentFromTo;
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
