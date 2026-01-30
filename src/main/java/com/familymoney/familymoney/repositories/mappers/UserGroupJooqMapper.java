package com.familymoney.familymoney.repositories.mappers;

import com.familymoney.familymoney.generated.tables.UserGroups;
import com.familymoney.familymoney.repositories.dbos.UserGroupDbo;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.UserId;
import java.time.OffsetDateTime;
import org.jooq.Record;

public final class UserGroupJooqMapper {

  private UserGroupJooqMapper() {}

  public static UserGroupDbo toDbo(final Record r) {
    OffsetDateTime joinedAt = r.get(UserGroups.USER_GROUPS.JOINED_AT);

    return UserGroupDbo.builder()
        .userId(UserId.fromUuid(r.get(UserGroups.USER_GROUPS.USER_ID)))
        .groupId(GroupId.fromUuid(r.get(UserGroups.USER_GROUPS.GROUP_ID)))
        .joinedAt(joinedAt != null ? joinedAt.toInstant() : null)
        .build();
  }
}
