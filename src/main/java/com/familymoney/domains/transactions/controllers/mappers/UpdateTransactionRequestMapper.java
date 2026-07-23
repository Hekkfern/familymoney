package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.UpdateTransactionRequestDto;
import com.familymoney.domains.transactions.services.data.UpdateTransactionData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.user.types.UserId;

public final class UpdateTransactionRequestMapper {

  private UpdateTransactionRequestMapper() {
    /* this class is not intended to be instantiated */
  }

  public static UpdateTransactionData fromDto(final UpdateTransactionRequestDto dto) {
    return new UpdateTransactionData(
        dto.description() != null ? Description.of(dto.description()) : null,
        dto.from() != null ? UserId.fromUuid(dto.from()) : null,
        dto.to() != null ? UserId.fromUuid(dto.to()) : null,
        dto.amount() != null ? dto.amount() : null,
        dto.doneAt() != null ? dto.doneAt() : null);
  }
}
