package com.familymoney.domains.users.repositories.dbos;

import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;

/**
 * DTO for creating a new user in the database.
 *
 * @param id the unique identifier for the user.
 * @param username the user's display/handle.
 * @param email the user's email address.
 * @param passwordHash the hashed password to store for the user. Cannot be empty.
 */
public record CreateUserDbo(
    UserId id,
    UserName username,
    Email email,
    String passwordHash,
    boolean isEnabled,
    boolean isEmailVerified) {

  public CreateUserDbo {
    assert !passwordHash.isEmpty() : "Password hash cannot be empty";
  }
}
