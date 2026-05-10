package com.familymoney.domains.user.repositories.dtos;

import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.domains.user.types.UserName;
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
public record CreateUserDto(UserId id, UserName username, Email email, String passwordHash) {
  public CreateUserDto {
    assert !passwordHash.isEmpty() : "Password hash cannot be empty";
  }
}
