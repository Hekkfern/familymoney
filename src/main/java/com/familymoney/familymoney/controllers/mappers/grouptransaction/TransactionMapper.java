package com.familymoney.familymoney.controllers.mappers.grouptransaction;

import com.familymoney.familymoney.controllers.dtos.grouptransaction.TransactionDto;
import com.familymoney.familymoney.services.data.TransactionData;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

  public TransactionDto toDto(TransactionData data) {
    return TransactionDto.builder()
        .id(data.id().value())
        .from(data.)
        .to()
        .amount()
        .description()
        .doneAt()
        .build();
  }
}
