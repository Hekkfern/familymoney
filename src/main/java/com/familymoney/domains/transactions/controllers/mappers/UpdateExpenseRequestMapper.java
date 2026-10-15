package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.UpdateExpenseRequestDto;
import com.familymoney.domains.transactions.services.data.UpdateExpenseData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.users.types.UserId;
import java.util.stream.Collectors;

public final class UpdateExpenseRequestMapper {

  private UpdateExpenseRequestMapper() {
    /* this class is not intended to be instantiated */
  }

  public static UpdateExpenseData fromDto(final UpdateExpenseRequestDto dto) {
    return new UpdateExpenseData(
        dto.description() != null ? Description.of(dto.description()) : null,
        dto.doneAt(),
        dto.shares() != null
            ? dto.shares().stream()
                .collect(
                    Collectors.toMap(
                        share -> UserId.fromUuid(share.userId()),
                        UpdateExpenseRequestDto.ExpenseShareDto::amount))
            : null,
        dto.payers() != null
            ? dto.payers().stream()
                .collect(
                    Collectors.toMap(
                        payer -> UserId.fromUuid(payer.userId()),
                        UpdateExpenseRequestDto.ExpensePayerDto::amount))
            : null);
  }
}
