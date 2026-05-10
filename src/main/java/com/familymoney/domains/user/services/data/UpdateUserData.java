package com.familymoney.domains.user.services.data;

import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.Password;
import com.familymoney.domains.user.types.UserName;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@Builder
public class UpdateUserData {
  @Nullable @Default private UserName username = null;
  @Nullable @Default private Email email = null;
  @Nullable @Default private Password password = null;

  public boolean isEmpty() {
    return username == null && email == null && password == null;
  }
}
