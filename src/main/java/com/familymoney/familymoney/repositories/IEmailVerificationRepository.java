package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dtos.CreateEmailVerificationDto;
import com.familymoney.familymoney.repositories.entities.EmailVerificationEntity;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.UserId;
import java.util.Optional;

public interface IEmailVerificationRepository {

  /**
   * Create a new email verification record
   *
   * @param data values to store
   * @return Created {@link EmailVerificationEntity} wrapped in Optional, or empty Optional if creation
   *     failed
   */
  Optional<EmailVerificationEntity> create(CreateEmailVerificationDto data);

  /**
   * Find an Email Verification record by its token
   *
   * @param token Email Verification token
   * @return Found EmailVerificationDbo wrapped in Optional, or empty Optional if not found
   */
  Optional<EmailVerificationEntity> findByToken(EmailVerificationToken token);

  /**
   * Delete Email Verification records by User ID
   *
   * @param userId ID of the user
   * @return true if deletion was successful, false otherwise
   */
  boolean deleteByUserId(UserId userId);
}
