package com.familymoney.familymoney.controllers.mappers.grouptransaction;

import com.familymoney.familymoney.controllers.dtos.grouptransaction.UpdateTransactionRequestDto;
import com.familymoney.familymoney.services.data.UpdateTransactionData;
import com.familymoney.familymoney.types.GroupName;
import org.springframework.stereotype.Component;

@Component
public class UpdateTransactionRequestMapper {

  public UpdateTransactionData fromDto(UpdateTransactionRequestDto dto) {
    return UpdateTransactionData.builder()
        .description(dto.description() != null ? dto.description() : null)
        .from(dto.name() != null ? GroupName.fromString(dto.name()) : null)
        .to(dto.name() != null ? GroupName.fromString(dto.name()) : null)
        .amount(dto.name() != null ? GroupName.fromString(dto.name()) : null)
        .build();
  }
}
