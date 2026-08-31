package com.familymoney.domains.auth.repositories;

import com.familymoney.domains.auth.repositories.dtos.CreatePasswordResetDto;
import com.familymoney.domains.auth.repositories.dtos.UpdatePasswordResetDto;
import com.familymoney.domains.auth.repositories.entitites.PasswordResetEntity;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.users.types.UserId;
import java.util.Optional;

public interface PasswordResetRepository {

  /**
   * Creates a password reset record.
   *
   * @param data password reset values to persist
   * @return the persisted password reset record, or empty when it could not be created
   */
  Optional<PasswordResetEntity> create(CreatePasswordResetDto data);

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
   * @return {@code true} when a password reset record was updated
   */
  boolean updateByUserId(UserId userId, UpdatePasswordResetDto data);

  /**
   * Deletes password reset records associated with the specified user ID. This method is typically
   * used to clean up password reset requests after they have been fulfilled or when they are no
   * longer valid.
   *
   * @param userId ID of the user whose password reset records should be deleted
   * @return true if the deletion was successful (i.e., records were found and deleted), or false if
   *     no matching records exist for the provided user ID
   */
  boolean deleteByUserId(UserId userId);
}
