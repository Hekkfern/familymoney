package com.familymoney.domains.auth.repositories.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.auth.repositories.entitites.UsedRefreshTokenEntity;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.generated.tables.records.UsedRefreshTokensRecord;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UsedRefreshTokenJooqMapperTest {

  @Nested
  class ToEntity {

    @Test
    void map_successfully() {
      final UUID familyUuid = UUID.randomUUID();
      final OffsetDateTime usedAt =
          OffsetDateTime.of(2026, 1, 1, 10, 0, 0, 0, ZoneOffset.ofHours(2));
      final UsedRefreshTokensRecord r = new UsedRefreshTokensRecord();
      r.setFamily(familyUuid);
      r.setUsedAt(usedAt);

      final UsedRefreshTokenEntity entity = UsedRefreshTokenJooqMapper.toEntity(r);

      assertThat(entity.family()).isEqualTo(TokenFamily.fromUuid(familyUuid));
      assertThat(entity.usedAt()).isEqualTo(usedAt.toInstant());
    }
  }
}
