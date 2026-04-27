package com.familymoney.controllers.dtos.grouptransaction;

import lombok.Builder;
import org.springframework.data.domain.Page;

@Builder
public record GetTransactionsResponseDto(Page<TransactionDto> transactions) {}
