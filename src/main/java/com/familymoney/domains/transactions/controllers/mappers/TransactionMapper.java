package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.services.data.TransactionData;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

  public TransactionDto toDto(TransactionData data) {
    return TransactionDto.builder()
        .id(data.id().value())
        .from(data.from().value())
        .to(data.to().value())
        .amount(data.amount())
        .description(data.description())
        .doneAt(data.doneAt())
        .build();
  }
}
