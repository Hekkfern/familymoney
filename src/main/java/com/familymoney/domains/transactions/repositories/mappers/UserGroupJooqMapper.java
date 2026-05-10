package com.familymoney.domains.transactions.repositories.mappers;

import com.familymoney.domains.transactions.repositories.entitites.UserGroupEntity;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.generated.tables.UserGroups;
import java.time.OffsetDateTime;
import org.jooq.Record;

public final class UserGroupJooqMapper {

  private UserGroupJooqMapper() {}

  public static UserGroupEntity toEntity(final Record r) {
    OffsetDateTime joinedAt = r.get(UserGroups.USER_GROUPS.JOINED_AT);

    return UserGroupEntity.builder()
        .userId(UserId.fromUuid(r.get(UserGroups.USER_GROUPS.USER_ID)))
        .groupId(GroupId.fromUuid(r.get(UserGroups.USER_GROUPS.GROUP_ID)))
        .joinedAt(joinedAt != null ? joinedAt.toInstant() : null)
        .build();
  }
}
