package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.Username;
import java.time.Duration;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserRepository {

  /**
   * Create a new user record
   *
   * @param username Nametag of the user
   * @param email Email address of the user
   * @param passwordHash Hashed password of the user
   * @return Created UserDbo wrapped in Optional, or empty Optional if creation failed
   */
  Optional<UserDbo> create(Username username, Email email, String passwordHash);

  /**
   * Find a user record by its ID
   *
   * @param id ID of the user
   * @return Found UserDbo wrapped in Optional, or empty Optional if not found
   */
  Optional<UserDbo> findById(UserId id);

  /**
   * Find a user record by its email
   *
   * @param email Email address of the user
   * @return Found UserDbo wrapped in Optional, or empty Optional if not found
   */
  Optional<UserDbo> findByEmail(Email email);

  /**
   * Find a user record by its username
   *
   * @param username Nametag of the user
   * @return Found UserDbo wrapped in Optional, or empty Optional if not found
   */
  Optional<UserDbo> findByUsername(Username username);

  /**
   * Check if there is any other user record with the given email or username
   *
   * @param email Email address to check
   * @param username Nametag to check
   * @return true if a user with the given email or username exists, false otherwise
   */
  boolean existsByEmailOrUsername(Email email, Username username);

  /**
   * Update user information such as username and email
   *
   * @param id ID of the user to update
   * @param username New username, or null if unchanged
   * @param email New email address, or null if unchanged
   * @return true if the update was successful, false otherwise
   */
  boolean updateInfo(UserId id, @Nullable Username username, @Nullable Email email);

  /**
   * Update the password hash for a user
   *
   * @param id ID of the user
   * @param newPasswordHash New hashed password
   * @return true if the update was successful, false otherwise
   */
  boolean updatePassword(UserId id, String newPasswordHash);

  /**
   * Delete a user record by its ID
   *
   * @param id ID of the user to delete
   * @return true if deletion was successful, false otherwise
   */
  boolean deleteById(UserId id);

  /**
   * Delete unverified user records older than the specified duration from the current time
   *
   * @param cutoff Duration to determine the age of records to delete
   */
  void deleteByIsUnverifiedAndOlderThan(Duration cutoff);

  /**
   * Set the enabled status for a user by their ID
   *
   * @param id ID of the user
   * @param isEnabled New enabled status
   * @return true if the update was successful, false otherwise
   */
  boolean setIsEnabledByUserId(UserId id, boolean isEnabled);

  /**
   * Verify the email address of a user by their ID
   *
   * @param userId ID of the user
   * @return true if the email was successfully verified, false otherwise
   */
  boolean verifyEmail(UserId userId);

  /**
   * Retrieve a paginated list of all users
   *
   * @param pageable Pagination information
   * @return A page of UserDbo records
   */
  Page<UserDbo> findAll(Pageable pageable);
}
