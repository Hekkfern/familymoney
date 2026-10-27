package com.familymoney.domains.auth.repositories;

import com.familymoney.domains.auth.repositories.dbos.CreateEmailVerificationDbo;
import com.familymoney.domains.auth.repositories.dbos.UpdateEmailVerificationTokenDbo;
import com.familymoney.domains.auth.repositories.entitites.EmailVerificationEntity;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.users.types.UserId;
import java.util.Optional;

public interface EmailVerificationRepository {

  /**
   * Create a new email verification record
   *
   * @param data values to store
   */
  void create(CreateEmailVerificationDbo data);

  /**
   * Find an entry by its User ID.
   *
   * @param userId the identifier of the user to find. Must not be null.
   * @return an {@link Optional} containing the {@link EmailVerificationEntity} if a user with the
   *     id exists, otherwise an empty Optional.
   */
  Optional<EmailVerificationEntity> findByUserId(UserId userId);

  /**
   * Find an Email Verification record by its token
   *
   * @param token Email Verification token
   * @return Found EmailVerificationDbo wrapped in Optional, or empty Optional if not found
   */
  Optional<EmailVerificationEntity> findByToken(EmailVerificationToken token);

  /**
   * Update one or more fields identified by its User ID.
   *
   * @param userId the id of the user to update. Must not be null.
   * @param data a {@link UpdateEmailVerificationTokenDbo} containing fields to change. Must not be
   *     null. Only non-null fields will be applied.
   * @throws com.familymoney.domains.auth.exceptions.VerificationTokenNotFoundException if the
   *     verification token does not exist
   */
  void updateByUserId(UserId userId, UpdateEmailVerificationTokenDbo data);

  /**
   * Delete Email Verification records by User ID
   *
   * @param userId ID of the user
   */
  void deleteByUserId(UserId userId);
}
