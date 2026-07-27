package com.familymoney.domains.users.repositories.dtos;

import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import lombok.Builder;

/**
 * DTO for creating a new user in the database.
 *
 * @param id the unique identifier for the user.
 * @param username the user's display/handle.
 * @param email the user's email address.
 * @param passwordHash the hashed password to store for the user. Cannot be empty.
 */
@Builder
public record CreateUserDto(
    UserId id,
    UserName username,
    Email email,
    String passwordHash,
    boolean isEnabled,
    boolean isEmailVerified) {
  public CreateUserDto {
    assert !passwordHash.isEmpty() : "Password hash cannot be empty";
  }
}
