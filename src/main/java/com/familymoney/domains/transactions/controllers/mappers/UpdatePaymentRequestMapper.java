package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.UpdatePaymentRequestDto;
import com.familymoney.domains.transactions.services.data.UpdatePaymentData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.users.types.UserId;

/** Maps payment update requests to service data. */
public final class UpdatePaymentRequestMapper {

  private UpdatePaymentRequestMapper() {
    /* this class is not intended to be instantiated */
  }

  /**
   * Maps a payment update request while preserving omitted fields.
   *
   * @param dto the payment update request
   * @return the payment update data
   */
  public static UpdatePaymentData fromDto(final UpdatePaymentRequestDto dto) {
    return new UpdatePaymentData(
        dto.description() != null ? Description.of(dto.description()) : null,
        dto.doneAt(),
        dto.amount(),
        dto.from() != null ? UserId.fromUuid(dto.from()) : null,
        dto.to() != null ? UserId.fromUuid(dto.to()) : null);
  }
}
