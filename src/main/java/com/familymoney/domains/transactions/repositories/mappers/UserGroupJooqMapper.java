package com.familymoney.domains.transactions.repositories.mappers;

import com.familymoney.domains.transactions.repositories.entitites.UserGroupEntity;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.UserGroups;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jooq.Record;

public final class UserGroupJooqMapper {

  private UserGroupJooqMapper() {
    /* this class is not intended to be instantiated */
  }

  public static UserGroupEntity toEntity(final Record r) {
    OffsetDateTime joinedAt = Objects.requireNonNull(r.get(UserGroups.USER_GROUPS.JOINED_AT));

    return new UserGroupEntity(
        UserId.fromUuid(r.get(UserGroups.USER_GROUPS.USER_ID)),
        GroupId.fromUuid(r.get(UserGroups.USER_GROUPS.GROUP_ID)),
        joinedAt.toInstant());
  }
}
