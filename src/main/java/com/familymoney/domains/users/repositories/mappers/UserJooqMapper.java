package com.familymoney.domains.users.repositories.mappers;

import com.familymoney.domains.users.repositories.entitites.UserEntity;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.generated.tables.Users;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jooq.Record;

public final class UserJooqMapper {

  private UserJooqMapper() {
    /* this class is not meant to be instantiated */
  }

  public static UserEntity toEntity(final Record r) {
    OffsetDateTime createdAt = Objects.requireNonNull(r.get(Users.USERS.CREATED_AT));
    OffsetDateTime updatedAt = Objects.requireNonNull(r.get(Users.USERS.UPDATED_AT));

    return new UserEntity(
        UserId.fromUuid(r.get(Users.USERS.ID)),
        UserName.fromString(r.get(Users.USERS.USERNAME)),
        Email.fromString(r.get(Users.USERS.EMAIL)),
        r.get(Users.USERS.HASHED_PASSWORD),
        createdAt.toInstant(),
        updatedAt.toInstant(),
        Boolean.TRUE.equals(r.get(Users.USERS.IS_EMAIL_VERIFIED)),
        Boolean.TRUE.equals(r.get(Users.USERS.IS_ENABLED)));
  }
}
