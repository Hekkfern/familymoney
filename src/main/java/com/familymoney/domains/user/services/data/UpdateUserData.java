package com.familymoney.domains.user.services.data;

import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.Password;
import com.familymoney.domains.user.types.UserName;
import org.jspecify.annotations.Nullable;

public record UpdateUserData(
    @Nullable UserName username, @Nullable Email email, @Nullable Password password) {

  public boolean isEmpty() {
    return username == null && email == null && password == null;
  }
}
