package com.familymoney.domains.transactions.repositories.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.records.GroupInvitationsRecord;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GroupInvitationJooqMapperTest {

  @Nested
  class ToEntity {

    @Test
    void map_successfully() {
      final UUID groupUuid = UUID.randomUUID();
      final UUID userUuid = UUID.randomUUID();
      final UUID createdByUuid = UUID.randomUUID();
      final OffsetDateTime expiresAt =
          OffsetDateTime.of(2026, 1, 2, 10, 0, 0, 0, ZoneOffset.ofHours(1));
      final OffsetDateTime lastSentAt = OffsetDateTime.of(2026, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
      final GroupInvitationsRecord r = new GroupInvitationsRecord();
      r.setGroupId(groupUuid);
      r.setUserId(userUuid);
      r.setCreatedBy(createdByUuid);
      r.setExpiresAt(expiresAt);
      r.setLastSentAt(lastSentAt);

      final GroupInvitationEntity entity = GroupInvitationJooqMapper.toEntity(r);

      assertThat(entity.groupId()).isEqualTo(GroupId.fromUuid(groupUuid));
      assertThat(entity.userId()).isEqualTo(UserId.fromUuid(userUuid));
      assertThat(entity.createdBy()).isEqualTo(UserId.fromUuid(createdByUuid));
      assertThat(entity.expiresAt().value()).isEqualTo(expiresAt.toInstant());
      assertThat(entity.lastSentAt()).isEqualTo(lastSentAt.toInstant());
    }
  }
}
