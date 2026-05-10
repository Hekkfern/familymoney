package com.familymoney.domains.auth.repositories.dtos;

import com.familymoney.domains.auth.types.EmailVerificationToken;
import java.time.Instant;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@Builder
public class UpdateEmailVerificationTokenDto {
  @Nullable @Default private EmailVerificationToken token = null;
  @Nullable @Default private Instant expiresAt = null;

  public boolean isEmpty() {
    return token == null && expiresAt == null;
  }
}
