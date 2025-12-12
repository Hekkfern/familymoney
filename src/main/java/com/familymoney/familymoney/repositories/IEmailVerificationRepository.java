package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.EmailVerificationDbo;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public interface IEmailVerificationRepository {

  Optional<EmailVerificationDbo> create(
      UserId userId, EmailVerificationToken token, Instant expiresAt);

  Optional<EmailVerificationDbo> findByToken(EmailVerificationToken token);

  void deleteByUserId(UserId userId);

  void deleteOlderThan(Duration cutoff);
}
