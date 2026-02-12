package com.familymoney.familymoney.repositories.dtos;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserName;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@Builder
public class UpdateUserDto {
  @Nullable @Default private UserName username = null;
  @Nullable @Default private Email email = null;
  @Nullable @Default private String hashedPassword = null;
  @Nullable @Default private Boolean isEmailVerified = null;
  @Nullable @Default private Boolean isEnabled = null;

  public boolean isEmpty() {
    return username == null
        && email == null
        && hashedPassword == null
        && isEmailVerified == null
        && isEnabled == null;
  }
}
