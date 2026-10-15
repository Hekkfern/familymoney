package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.PaymentDto;
import com.familymoney.domains.transactions.services.data.PaymentData;

/** Maps payment service data to response DTOs. */
public final class PaymentDtoMapper {

  private PaymentDtoMapper() {
    /* this class is not intended to be instantiated */
  }

  /**
   * Maps payment data to a response DTO.
   *
   * @param data the payment service data
   * @return the payment response DTO
   */
  public static PaymentDto toDto(final PaymentData data) {
    return new PaymentDto(
        data.id().value(),
        data.groupId().value(),
        data.description().value(),
        data.amount(),
        data.amount().getCurrency().getCurrencyCode(),
        data.doneAt(),
        data.createdBy().value(),
        data.fromUserId().value(),
        data.toUserId().value());
  }
}
