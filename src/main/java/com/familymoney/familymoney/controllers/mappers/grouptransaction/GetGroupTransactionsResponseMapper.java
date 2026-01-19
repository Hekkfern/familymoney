package com.familymoney.familymoney.controllers.mappers.grouptransaction;

import com.familymoney.familymoney.controllers.dtos.grouptransaction.GetTransactionsResponseDto;
import com.familymoney.familymoney.services.data.TransactionData;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class GetGroupTransactionsResponseMapper {

  public GetTransactionsResponseDto toDto(Page<TransactionData> transactions) {
    return GetTransactionsResponseDto.builder().transactions(transactionPages).build();
  }
}
