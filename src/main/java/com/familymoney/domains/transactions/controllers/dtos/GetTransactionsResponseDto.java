package com.familymoney.domains.transactions.controllers.dtos;

import lombok.Builder;
import org.springframework.data.domain.Page;

@Builder
public record GetTransactionsResponseDto(Page<TransactionDto> transactions) {}
