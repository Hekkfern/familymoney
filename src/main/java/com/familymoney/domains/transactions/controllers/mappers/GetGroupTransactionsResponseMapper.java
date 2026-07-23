package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.GetTransactionsResponseDto;
import com.familymoney.domains.transactions.services.data.TransactionData;
import org.springframework.data.domain.Page;

public final class GetGroupTransactionsResponseMapper {

  private GetGroupTransactionsResponseMapper() {
    /* this class is not intended to be instantiated */
  }

  public static GetTransactionsResponseDto toDto(final Page<TransactionData> transactions) {
    return new GetTransactionsResponseDto(transactions.map(TransactionMapper::toDto));
  }
}
