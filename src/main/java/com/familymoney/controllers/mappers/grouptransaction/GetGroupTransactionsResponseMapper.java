package com.familymoney.controllers.mappers.grouptransaction;

import com.familymoney.controllers.dtos.grouptransaction.GetTransactionsResponseDto;
import com.familymoney.services.data.TransactionData;
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
