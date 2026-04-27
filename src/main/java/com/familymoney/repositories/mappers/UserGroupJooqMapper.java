package com.familymoney.repositories.mappers;

import com.familymoney.generated.tables.UserGroups;
import com.familymoney.repositories.entities.UserGroupEntity;
import com.familymoney.types.GroupId;
import com.familymoney.types.UserId;
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
