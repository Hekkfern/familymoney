package com.familymoney.domains.user.controllers.mappers;

import com.familymoney.domains.user.controllers.dtos.UpdateUserRequestDto;
import com.familymoney.domains.user.services.data.UpdateUserData;
import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.Password;
import com.familymoney.domains.user.types.UserName;

public final class UpdateUserRequestMapper {

  private UpdateUserRequestMapper() {
    /* this class is not meant to be instantiated */
  }

  public static UpdateUserData fromDto(final UpdateUserRequestDto dto) {
    return new UpdateUserData(
        dto.username() != null ? UserName.fromString(dto.username()) : null,
        dto.email() != null ? Email.fromString(dto.email()) : null,
        dto.password() != null ? Password.fromString(dto.password()) : null);
  }
}
