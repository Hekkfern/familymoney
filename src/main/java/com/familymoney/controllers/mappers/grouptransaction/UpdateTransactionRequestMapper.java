package com.familymoney.controllers.mappers.grouptransaction;

import com.familymoney.controllers.dtos.grouptransaction.UpdateTransactionRequestDto;
import com.familymoney.services.data.UpdateTransactionData;
import com.familymoney.types.UserId;
import org.springframework.stereotype.Component;

@Component
public class UpdateTransactionRequestMapper {

  public UpdateTransactionData fromDto(UpdateTransactionRequestDto dto) {
    return UpdateTransactionData.builder()
        .description(dto.description() != null ? dto.description() : null)
        .from(dto.from() != null ? UserId.fromUuid(dto.from()) : null)
        .to(dto.to() != null ? UserId.fromUuid(dto.to()) : null)
        .amount(dto.amount() != null ? dto.amount() : null)
        .doneAt(dto.doneAt() != null ? dto.doneAt() : null)
        .build();
  }
}
