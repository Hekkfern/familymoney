package com.familymoney.domains.transactions.services.mappers;

import com.familymoney.domains.transactions.repositories.dtos.UpdateTransactionDto;
import com.familymoney.domains.transactions.services.data.UpdateTransactionData;

public class UpdateTransactionDataMapper {

  private UpdateTransactionDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static UpdateTransactionDto toDbo(UpdateTransactionData data) {
    return UpdateTransactionDto.builder()
        .amount(data.getAmount())
        .description(data.getDescription())
        .from(data.getFrom())
        .to(data.getTo())
        .doneAt(data.getDoneAt())
        .build();
  }
}
