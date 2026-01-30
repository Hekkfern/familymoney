package com.familymoney.familymoney.repositories.mappers;

import com.familymoney.familymoney.generated.tables.Users;
import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.UserName;
import java.time.OffsetDateTime;
import org.jooq.Record;

public final class UserJooqMapper {

  private UserJooqMapper() {}

  public static UserDbo toDbo(final Record r) {
    OffsetDateTime createdAt = r.get(Users.USERS.CREATED_AT);
    OffsetDateTime updatedAt = r.get(Users.USERS.UPDATED_AT);

    return UserDbo.builder()
        .id(UserId.fromUuid(r.get(Users.USERS.ID)))
        .username(UserName.fromString(r.get(Users.USERS.USERNAME)))
        .email(Email.fromString(r.get(Users.USERS.EMAIL)))
        .hashedPassword(r.get(Users.USERS.HASHED_PASSWORD))
        .createdAt(createdAt != null ? createdAt.toInstant() : null)
        .updatedAt(updatedAt != null ? updatedAt.toInstant() : null)
        .isEmailVerified(Boolean.TRUE.equals(r.get(Users.USERS.IS_EMAIL_VERIFIED)))
        .isEnabled(Boolean.TRUE.equals(r.get(Users.USERS.IS_ENABLED)))
        .build();
  }
}
