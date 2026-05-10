package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.UpdateTransactionRequestDto;
import com.familymoney.domains.transactions.services.data.UpdateTransactionData;
import com.familymoney.domains.user.types.UserId;
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
