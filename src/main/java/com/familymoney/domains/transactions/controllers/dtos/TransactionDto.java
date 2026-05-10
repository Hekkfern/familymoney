package com.familymoney.domains.transactions.controllers.dtos;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import org.javamoney.moneta.Money;

@Builder
public record TransactionDto(
    UUID id, UUID from, UUID to, Money amount, String description, Instant doneAt) {}
