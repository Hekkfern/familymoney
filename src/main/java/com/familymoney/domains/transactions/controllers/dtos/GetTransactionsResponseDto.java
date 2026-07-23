package com.familymoney.domains.transactions.controllers.dtos;

import org.springframework.data.domain.Page;

public record GetTransactionsResponseDto(Page<TransactionDto> transactions) {}
