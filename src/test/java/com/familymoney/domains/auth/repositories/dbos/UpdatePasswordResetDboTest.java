package com.familymoney.domains.auth.repositories.dbos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.auth.types.PasswordResetToken;
import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UpdatePasswordResetDboTest {

  private static final Instant INSTANT = Instant.parse("2026-01-01T00:00:00Z");

  @Nested
  class Constructor {

    @Test
    void rejects_update_without_any_field() {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> new UpdatePasswordResetDbo(null, null, null));
    }

    @Test
    void accepts_update_with_only_token() {
      final PasswordResetToken token = PasswordResetToken.generate();

      final UpdatePasswordResetDbo dbo = new UpdatePasswordResetDbo(token, null, null);

      assertThat(dbo.token()).isEqualTo(token);
    }

    @Test
    void accepts_update_with_only_expiration_time() {
      final ExpirationTime expiresAt = ExpirationTime.of(INSTANT);

      final UpdatePasswordResetDbo dbo = new UpdatePasswordResetDbo(null, expiresAt, null);

      assertThat(dbo.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void accepts_update_with_only_last_sent_at() {
      final UpdatePasswordResetDbo dbo = new UpdatePasswordResetDbo(null, null, INSTANT);

      assertThat(dbo.lastSentAt()).isEqualTo(INSTANT);
    }

    @Test
    void accepts_update_with_all_fields() {
      final PasswordResetToken token = PasswordResetToken.generate();
      final ExpirationTime expiresAt = ExpirationTime.of(INSTANT);

      final UpdatePasswordResetDbo dbo =
          UpdatePasswordResetDbo.builder()
              .token(token)
              .expiresAt(expiresAt)
              .lastSentAt(INSTANT)
              .build();

      assertThat(dbo.token()).isEqualTo(token);
      assertThat(dbo.expiresAt()).isEqualTo(expiresAt);
      assertThat(dbo.lastSentAt()).isEqualTo(INSTANT);
    }
  }
}
