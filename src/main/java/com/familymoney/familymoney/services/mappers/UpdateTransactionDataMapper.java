package com.familymoney.familymoney.services.mappers;

import com.familymoney.familymoney.repositories.dtos.UpdateTransactionDto;
import com.familymoney.familymoney.services.data.UpdateTransactionData;

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
