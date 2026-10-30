package com.familymoney.domains.users.services.data;

import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Password;
import com.familymoney.domains.users.types.UserName;
import org.jspecify.annotations.Nullable;

public record UpdateUserData(
    @Nullable UserName username, @Nullable Email email, @Nullable Password password) {

  public UpdateUserData {
    final boolean isEmptyUpdate = username == null && email == null && password == null;
    if (isEmptyUpdate) {
      throw new IllegalArgumentException("At least one field must be provided for update");
    }
  }

  public boolean isEmpty() {
    return username == null && email == null && password == null;
  }
}
