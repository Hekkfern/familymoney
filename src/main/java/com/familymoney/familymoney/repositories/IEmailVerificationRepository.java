package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.EmailVerificationDbo;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

public interface IEmailVerificationRepository {

  @NonNull
  Optional<EmailVerificationDbo> create(
      @NonNull UserId userId, @NonNull EmailVerificationToken token, @NonNull Instant expiresAt);

  @NonNull
  Optional<EmailVerificationDbo> findByToken(@NonNull EmailVerificationToken token);

  void deleteByUserId(@NonNull UserId userId);

  void verifyEmail(@NonNull UserId userId);

  void deleteOlderThan(@NonNull Duration cutoff);
}
