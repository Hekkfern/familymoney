package com.familymoney.domains.auth.repositories.dbos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.ExpirationTime;
import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UpdateEmailVerificationTokenDboTest {

  private static final Instant INSTANT = Instant.parse("2026-01-01T00:00:00Z");

  @Nested
  class Constructor {

    @Test
    void rejects_update_without_any_field() {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> new UpdateEmailVerificationTokenDbo(null, null, null));
    }

    @Test
    void accepts_update_with_only_token() {
      final EmailVerificationToken token = EmailVerificationToken.generate();

      final UpdateEmailVerificationTokenDbo dbo =
          new UpdateEmailVerificationTokenDbo(token, null, null);

      assertThat(dbo.token()).isEqualTo(token);
    }

    @Test
    void accepts_update_with_only_expiration_time() {
      final ExpirationTime expiresAt = ExpirationTime.of(INSTANT);

      final UpdateEmailVerificationTokenDbo dbo =
          new UpdateEmailVerificationTokenDbo(null, expiresAt, null);

      assertThat(dbo.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void accepts_update_with_only_last_sent_at() {
      final UpdateEmailVerificationTokenDbo dbo =
          new UpdateEmailVerificationTokenDbo(null, null, INSTANT);

      assertThat(dbo.lastSentAt()).isEqualTo(INSTANT);
    }

    @Test
    void accepts_update_with_all_fields() {
      final EmailVerificationToken token = EmailVerificationToken.generate();
      final ExpirationTime expiresAt = ExpirationTime.of(INSTANT);

      final UpdateEmailVerificationTokenDbo dbo =
          UpdateEmailVerificationTokenDbo.builder()
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
