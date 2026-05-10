package com.familymoney.domains.user.controllers.mappers;

import com.familymoney.domains.user.controllers.dtos.UpdateUserRequestDto;
import com.familymoney.domains.user.services.data.UpdateUserData;
import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.Password;
import com.familymoney.domains.user.types.UserName;
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
