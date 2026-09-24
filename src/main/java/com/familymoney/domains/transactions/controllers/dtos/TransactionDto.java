package com.familymoney.domains.transactions.controllers.dtos;

import java.util.UUID;

public record TransactionDto(TransactionType type, UUID id) {}
