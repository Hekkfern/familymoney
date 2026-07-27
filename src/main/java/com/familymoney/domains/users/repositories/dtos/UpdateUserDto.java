package com.familymoney.domains.users.repositories.dtos;

import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserName;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateUserDto(
    @Nullable UserName username,
    @Nullable Email email,
    @Nullable String hashedPassword,
    @Nullable Boolean isEmailVerified,
    @Nullable Boolean isEnabled) {

  public boolean isEmpty() {
    return username == null
        && email == null
        && hashedPassword == null
        && isEmailVerified == null
        && isEnabled == null;
  }
}
