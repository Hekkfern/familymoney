package com.familymoney.domains.users.repositories;

import com.familymoney.domains.users.repositories.dtos.CreateUserDto;
import com.familymoney.domains.users.repositories.dtos.UpdateUserDto;
import com.familymoney.domains.users.repositories.entitites.UserEntity;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository contract for user persistence operations.
 *
 * <p>Implementations are responsible for CRUD operations and queries related to application users.
 * Methods use domain-specific DTOs (Dbo) and value objects for strong typing.
 */
public interface IUserRepository {

  /**
   * Store a new user.
   *
   * @param data values to store.
   * @return an {@link Optional} containing the created {@link UserEntity} when successful; empty
   *     Optional if the creation failed (e.g., uniqueness violation).
   */
  Optional<UserEntity> create(CreateUserDto data);

  /**
   * Find a user by its identifier.
   *
   * @param id the identifier of the user to find. Must not be null.
   * @return an {@link Optional} containing the {@link UserEntity} if a user with the id exists,
   *     otherwise an empty Optional.
   */
  Optional<UserEntity> findById(UserId id);

  /**
   * Find a user by its email address.
   *
   * @param email the email address to search for. Must not be null.
   * @return an {@link Optional} containing the {@link UserEntity} if found, otherwise empty.
   */
  Optional<UserEntity> findByEmail(Email email);

  /**
   * Find a user by its username.
   *
   * @param username the username to search for. Must not be null.
   * @return an {@link Optional} containing the {@link UserEntity} if found, otherwise empty.
   */
  Optional<UserEntity> findByUsername(UserName username);

  /**
   * Check whether any user exists with the given email or username.
   *
   * <p>Typical implementations use this to guard uniqueness before creation or update. Note that
   * there may still be a race condition: callers should handle uniqueness constraint violations
   * from the database in addition to using this check.
   *
   * @param email the email to check for. Must not be null.
   * @param username the username to check for. Must not be null.
   * @return true if a record with the given email or username exists, false otherwise.
   */
  boolean existsByEmailOrUsername(Email email, UserName username);

  /**
   * Check whether a user exists by its id.
   *
   * @param id the id to check for. Must not be null.
   * @return true if a user with the provided id exists, false otherwise.
   */
  boolean existsById(UserId id);

  /**
   * Update one or more fields of a user identified by its id.
   *
   * @param id the id of the user to update. Must not be null.
   * @param data a {@link UpdateUserDto} containing fields to change. Must not be null. Only
   *     non-null fields will be applied.
   * @return true if the update affected an existing record, false otherwise.
   */
  boolean updateById(UserId id, UpdateUserDto data);

  /**
   * Delete a user by its id.
   *
   * @param id the id of the user to delete. Must not be null.
   * @return true if a record was deleted, false if no matching record existed.
   */
  boolean deleteById(UserId id);

  /**
   * Retrieve a paginated list of users.
   *
   * @param pageable paging information (page number, size, sort). Must not be null.
   * @return a {@link Page} of {@link UserEntity} containing the users for the requested page. If no
   *     users exist, the page will be empty.
   */
  Page<UserEntity> getAll(Pageable pageable);
}
