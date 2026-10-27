package com.familymoney.domains.auth.repositories;

import com.familymoney.domains.auth.exceptions.ResetPasswordTokenNotFoundException;
import com.familymoney.domains.auth.repositories.dbos.CreatePasswordResetDbo;
import com.familymoney.domains.auth.repositories.dbos.UpdatePasswordResetDbo;
import com.familymoney.domains.auth.repositories.entitites.PasswordResetEntity;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.users.types.UserId;
import java.util.Optional;

public interface PasswordResetRepository {

  /**
   * Creates a password reset record.
   *
   * @param data password reset values to persist
   */
  void create(CreatePasswordResetDbo data);

  /**
   * Finds a password reset record by its user identifier.
   *
   * @param userId user identifier associated with the reset token
   * @return the password reset record, or empty when none exists
   */
  Optional<PasswordResetEntity> findByUserId(UserId userId);

  /**
   * Retrieves a password reset record based on the provided password reset token.
   *
   * @param token the unique token associated with the password reset request
   * @return an {@link Optional} containing the found PasswordResetDbo if a matching record is
   *     found, or an empty {@link Optional} if no matching record exists for the provided token
   */
  Optional<PasswordResetEntity> findByToken(PasswordResetToken token);

  /**
   * Replaces the token values associated with a user identifier.
   *
   * @param userId user identifier associated with the reset token
   * @param data replacement password reset values
   * @throws ResetPasswordTokenNotFoundException if the user has no password reset record
   */
  void updateByUserId(UserId userId, UpdatePasswordResetDbo data);

  /**
   * Deletes password reset records associated with the specified user ID. This method is typically
   * used to clean up password reset requests after they have been fulfilled or when they are no
   * longer valid.
   *
   * @param userId ID of the user whose password reset records should be deleted
   */
  void deleteByUserId(UserId userId);
}
