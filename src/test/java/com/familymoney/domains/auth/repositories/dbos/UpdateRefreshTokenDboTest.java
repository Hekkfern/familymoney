package com.familymoney.domains.auth.repositories.dbos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.auth.types.RefreshToken;
import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UpdateRefreshTokenDboTest {

  private static final Instant INSTANT = Instant.parse("2026-01-01T00:00:00Z");

  @Nested
  class Constructor {

    @Test
    void rejects_update_without_any_field() {
      assertThatIllegalArgumentException().isThrownBy(() -> new UpdateRefreshTokenDbo(null, null));
    }

    @Test
    void accepts_update_with_only_token() {
      final RefreshToken token = RefreshToken.generate();

      final UpdateRefreshTokenDbo dbo = new UpdateRefreshTokenDbo(token, null);

      assertThat(dbo.token()).isEqualTo(token);
    }

    @Test
    void accepts_update_with_only_expiration_time() {
      final ExpirationTime expiresAt = ExpirationTime.of(INSTANT);

      final UpdateRefreshTokenDbo dbo = new UpdateRefreshTokenDbo(null, expiresAt);

      assertThat(dbo.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void accepts_update_with_all_fields() {
      final RefreshToken token = RefreshToken.generate();
      final ExpirationTime expiresAt = ExpirationTime.of(INSTANT);

      final UpdateRefreshTokenDbo dbo =
          UpdateRefreshTokenDbo.builder().token(token).expiresAt(expiresAt).build();

      assertThat(dbo.token()).isEqualTo(token);
      assertThat(dbo.expiresAt()).isEqualTo(expiresAt);
    }
  }
}
