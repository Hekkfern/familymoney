package com.familymoney.repositories;

import com.familymoney.repositories.dtos.CreatePasswordResetDto;
import com.familymoney.repositories.entities.PasswordResetEntity;
import com.familymoney.types.PasswordResetToken;
import com.familymoney.types.UserId;
import java.util.Optional;

public interface IPasswordResetRepository {

  /**
   * Creates a new password reset record for a specific user with the provided token and expiration
   * time.
   *
   * @param data values to store
   * @return an {@link Optional} containing the created PasswordResetDbo if the creation was
   *     successful, or an empty {@link Optional} if the creation failed (e.g., due to invalid input
   *     or database constraints)
   */
  Optional<PasswordResetEntity> create(CreatePasswordResetDto data);

  /**
   * Retrieves a password reset record based on the provided password reset token.
   *
   * @param token the unique token associated with the password reset request
   * @return an {@link Optional} containing the found PasswordResetDbo if a matching record is
   *     found, or an empty {@link Optional} if no matching record exists for the provided token
   */
  Optional<PasswordResetEntity> findByToken(PasswordResetToken token);

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
