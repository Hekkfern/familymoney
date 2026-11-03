package com.familymoney.domains.transactions.repositories.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.repositories.entitites.UserGroupEntity;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.records.UserGroupsRecord;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserGroupJooqMapperTest {

  @Nested
  class ToEntity {

    @Test
    void map_successfully() {
      final UUID userUuid = UUID.randomUUID();
      final UUID groupUuid = UUID.randomUUID();
      final OffsetDateTime joinedAt =
          OffsetDateTime.of(2026, 1, 2, 10, 0, 0, 0, ZoneOffset.ofHours(1));
      final UserGroupsRecord r = new UserGroupsRecord();
      r.setUserId(userUuid);
      r.setGroupId(groupUuid);
      r.setJoinedAt(joinedAt);

      final UserGroupEntity entity = UserGroupJooqMapper.toEntity(r);

      assertThat(entity.userId()).isEqualTo(UserId.fromUuid(userUuid));
      assertThat(entity.groupId()).isEqualTo(GroupId.fromUuid(groupUuid));
      assertThat(entity.joinedAt()).isEqualTo(joinedAt.toInstant());
    }
  }
}
