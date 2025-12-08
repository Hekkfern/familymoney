package com.familymoney.familymoney.controllers.mappers;

import com.familymoney.familymoney.controllers.dtos.user.UpdateUserRequestDto;
import com.familymoney.familymoney.services.data.UpdateUserData;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Password;
import com.familymoney.familymoney.types.Username;
import java.util.Optional;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class UpdateUserRequestMapper {

  public UpdateUserData fromDto(UpdateUserRequestDto dto) {
    return new UpdateUserData(
        dto.username() != null ? Optional.of(dto.username()).map(Username::new) : Optional.empty(),
        dto.email() != null ? Optional.of(dto.email()).map(Email::new) : Optional.empty(),
        dto.password() != null ? Optional.of(dto.password()).map(Password::new) : Optional.empty());
  }
}
