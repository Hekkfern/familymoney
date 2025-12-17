package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.PasswordResetDbo;
import com.familymoney.familymoney.types.PasswordResetToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import java.util.Optional;

public interface IPasswordResetRepository {

  Optional<PasswordResetDbo> create(UserId userId, PasswordResetToken token, Instant expiresAt);

  Optional<PasswordResetDbo> findByToken(PasswordResetToken token);

  boolean deleteByUserId(UserId userId);
}
