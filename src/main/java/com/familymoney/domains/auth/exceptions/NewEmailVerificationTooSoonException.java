package com.familymoney.domains.auth.exceptions;

import java.time.Instant;
import lombok.Getter;

@Getter
public final class NewEmailVerificationTooSoonException extends RuntimeException {

  private final Instant nextRequestAt;

  public NewEmailVerificationTooSoonException(final Instant nextRequestAt) {
    this.nextRequestAt = nextRequestAt;
  }
}
