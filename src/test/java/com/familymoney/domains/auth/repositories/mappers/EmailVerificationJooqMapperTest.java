package com.familymoney.domains.auth.repositories.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.auth.repositories.entitites.EmailVerificationEntity;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.records.EmailVerificationTokensRecord;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EmailVerificationJooqMapperTest {

  @Nested
  class ToEntity {

    @Test
    void map_successfully() {
      final UUID userId = UUID.randomUUID();
      final OffsetDateTime expiresAt =
          OffsetDateTime.of(2026, 1, 2, 10, 0, 0, 0, ZoneOffset.ofHours(1));
      final OffsetDateTime lastSentAt = OffsetDateTime.of(2026, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
      final EmailVerificationTokensRecord r = new EmailVerificationTokensRecord();
      r.setUserId(userId);
      r.setExpiresAt(expiresAt);
      r.setLastSentAt(lastSentAt);

      final EmailVerificationEntity entity = EmailVerificationJooqMapper.toEntity(r);

      assertThat(entity.userId()).isEqualTo(UserId.fromUuid(userId));
      assertThat(entity.expiresAt().value()).isEqualTo(expiresAt.toInstant());
      assertThat(entity.lastSentAt()).isEqualTo(lastSentAt.toInstant());
    }
  }
}
