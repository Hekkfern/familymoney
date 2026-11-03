package com.familymoney.domains.auth.repositories.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.auth.repositories.entitites.PasswordResetEntity;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.records.PasswordResetTokensRecord;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PasswordResetJooqMapperTest {

  @Nested
  class ToEntity {

    @Test
    void map_successfully() {
      final UUID userUuid = UUID.randomUUID();
      final OffsetDateTime expiresAt =
          OffsetDateTime.of(2026, 1, 2, 10, 0, 0, 0, ZoneOffset.ofHours(1));
      final OffsetDateTime lastSentAt = OffsetDateTime.of(2026, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
      final PasswordResetTokensRecord r = new PasswordResetTokensRecord();
      r.setUserId(userUuid);
      r.setExpiresAt(expiresAt);
      r.setLastSentAt(lastSentAt);

      final PasswordResetEntity entity = PasswordResetJooqMapper.toEntity(r);

      assertThat(entity.userId()).isEqualTo(UserId.fromUuid(userUuid));
      assertThat(entity.expiresAt().value()).isEqualTo(expiresAt.toInstant());
      assertThat(entity.lastSentAt()).isEqualTo(lastSentAt.toInstant());
    }
  }
}
