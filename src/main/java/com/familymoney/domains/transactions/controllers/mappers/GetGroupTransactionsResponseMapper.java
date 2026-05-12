package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.GetTransactionsResponseDto;
import com.familymoney.domains.transactions.services.data.TransactionData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetGroupTransactionsResponseMapper {

  private final TransactionMapper transactionMapper;

  public GetTransactionsResponseDto toDto(Page<TransactionData> transactions) {
    return GetTransactionsResponseDto.builder()
        .transactions(transactions.map(transactionMapper::toDto))
        .build();
  }
}
