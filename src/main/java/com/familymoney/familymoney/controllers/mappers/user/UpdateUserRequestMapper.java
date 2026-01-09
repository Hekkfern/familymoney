package com.familymoney.familymoney.controllers.mappers.user;

import com.familymoney.familymoney.controllers.dtos.user.UpdateUserRequestDto;
import com.familymoney.familymoney.services.data.UpdateUserData;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Password;
import com.familymoney.familymoney.types.UserName;
import org.springframework.stereotype.Component;

@Component
public class UpdateUserRequestMapper {

  public UpdateUserData fromDto(UpdateUserRequestDto dto) {
    return UpdateUserData.builder()
        .username(dto.username() != null ? UserName.fromString(dto.username()) : null)
        .email(dto.email() != null ? Email.fromString(dto.email()) : null)
        .password(dto.password() != null ? Password.fromString(dto.password()) : null)
        .build();
  }
}
