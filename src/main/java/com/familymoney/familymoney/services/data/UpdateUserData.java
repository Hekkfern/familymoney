package com.familymoney.familymoney.services.data;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Password;
import com.familymoney.familymoney.types.Username;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@Builder
public class UpdateUserData {
  @Nullable @Default private Username username = null;
  @Nullable @Default private Email email = null;
  @Nullable @Default private Password password = null;

  public boolean isEmpty() {
    return username == null && email == null && password == null;
  }
}
