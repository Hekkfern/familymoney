package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.EmailVerificationDbo;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public interface IEmailVerificationRepository {

  /**
   * Create a new email verification record
   *
   * @param userId ID of the user
   * @param token Email Verification token
   * @param expiresAt Timestamp when the token expires
   * @return Created EmailVerificationDbo wrapped in Optional, or empty Optional if creation failed
   */
  Optional<EmailVerificationDbo> create(
      UserId userId, EmailVerificationToken token, Instant expiresAt);

  /**
   * Find an Email Verification record by its token
   *
   * @param token Email Verification token
   * @return Found EmailVerificationDbo wrapped in Optional, or empty Optional if not found
   */
  Optional<EmailVerificationDbo> findByToken(EmailVerificationToken token);

  /**
   * Delete Email Verification records by User ID
   *
   * @param userId ID of the user
   * @return true if deletion was successful, false otherwise
   */
  boolean deleteByUserId(UserId userId);

  /**
   * Delete Email Verification records older than the specified duration from the current time
   *
   * @param cutoff Duration to determine the age of records to delete
   */
  void deleteOlderThan(Duration cutoff);
}
