package com.familymoney.domains.users.repositories.dbos;

import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserName;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateUserDbo(
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
