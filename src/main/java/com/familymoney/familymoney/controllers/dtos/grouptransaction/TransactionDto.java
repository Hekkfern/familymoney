package com.familymoney.familymoney.controllers.dtos.grouptransaction;

import java.time.Instant;
import java.util.UUID;
import org.javamoney.moneta.Money;

public record TransactionDto(
    UUID id, UUID from, UUID to, Money amount, String description, Instant doneAt) {}
