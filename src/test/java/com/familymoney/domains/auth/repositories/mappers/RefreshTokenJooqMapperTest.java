package com.familymoney.domains.auth.repositories.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.auth.repositories.entitites.RefreshTokenEntity;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.records.RefreshTokensRecord;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RefreshTokenJooqMapperTest {

  @Nested
  class ToEntity {

    @Test
    void map_successfully() {
      final UUID id = UUID.randomUUID();
      final UUID userUuid = UUID.randomUUID();
      final UUID familyUuid = UUID.randomUUID();
      final OffsetDateTime expiresAt =
          OffsetDateTime.of(2026, 1, 2, 10, 0, 0, 0, ZoneOffset.ofHours(1));
      final RefreshTokensRecord r = new RefreshTokensRecord();
      r.setId(id);
      r.setUserId(userUuid);
      r.setExpiresAt(expiresAt);
      r.setFamily(familyUuid);

      final RefreshTokenEntity entity = RefreshTokenJooqMapper.toEntity(r);

      assertThat(entity.id()).isEqualTo(id);
      assertThat(entity.userId()).isEqualTo(UserId.fromUuid(userUuid));
      assertThat(entity.expiresAt().value()).isEqualTo(expiresAt.toInstant());
      assertThat(entity.family()).isEqualTo(TokenFamily.fromUuid(familyUuid));
    }
  }
}
