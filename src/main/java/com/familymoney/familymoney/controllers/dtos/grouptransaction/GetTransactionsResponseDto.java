package com.familymoney.familymoney.controllers.dtos.grouptransaction;

import java.util.List;
import lombok.Builder;

@Builder
public record GetTransactionsResponseDto(List<TransactionDto> transactions) {}
