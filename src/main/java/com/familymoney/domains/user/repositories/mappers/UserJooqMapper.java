package com.familymoney.domains.user.repositories.mappers;

import com.familymoney.domains.user.repositories.entitites.UserEntity;
import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.domains.user.types.UserName;
import com.familymoney.generated.tables.Users;
import java.time.OffsetDateTime;
import org.jooq.Record;

public final class UserJooqMapper {

  private UserJooqMapper() {}

  public static UserEntity toEntity(final Record r) {
    OffsetDateTime createdAt = r.get(Users.USERS.CREATED_AT);
    OffsetDateTime updatedAt = r.get(Users.USERS.UPDATED_AT);

    return UserEntity.builder()
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
