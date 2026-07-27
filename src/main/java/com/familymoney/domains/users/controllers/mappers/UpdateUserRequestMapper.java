package com.familymoney.domains.users.controllers.mappers;

import com.familymoney.domains.users.controllers.dtos.UpdateUserRequestDto;
import com.familymoney.domains.users.services.data.UpdateUserData;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Password;
import com.familymoney.domains.users.types.UserName;

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
