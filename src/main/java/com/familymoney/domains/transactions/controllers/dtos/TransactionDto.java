package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.services.data.TransactionType;

import java.util.UUID;

public record TransactionDto(TransactionType type, UUID id) {}
