package com.familymoney.domains.users.repositories.dbos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.testutils.FakeGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UpdateUserDboTest {

  @Nested
  class Constructor {

    @Test
    void rejects_update_without_any_field() {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> new UpdateUserDbo(null, null, null, null, null));
    }

    @Test
    void accepts_update_with_only_username() {
      final UserName username = UserName.fromString(FakeGenerator.username());

      final UpdateUserDbo dbo = UpdateUserDbo.builder().username(username).build();

      assertThat(dbo.username()).isEqualTo(username);
    }

    @Test
    void accepts_update_with_only_email() {
      final Email email = Email.fromString(FakeGenerator.email());

      final UpdateUserDbo dbo = UpdateUserDbo.builder().email(email).build();

      assertThat(dbo.email()).isEqualTo(email);
    }

    @Test
    void accepts_update_with_only_hashed_password() {
      final UpdateUserDbo dbo = UpdateUserDbo.builder().hashedPassword("hash").build();

      assertThat(dbo.hashedPassword()).isEqualTo("hash");
    }

    @Test
    void accepts_update_with_only_email_verified_flag_set_to_false() {
      final UpdateUserDbo dbo = UpdateUserDbo.builder().isEmailVerified(false).build();

      assertThat(dbo.isEmailVerified()).isFalse();
    }

    @Test
    void accepts_update_with_only_enabled_flag_set_to_false() {
      final UpdateUserDbo dbo = UpdateUserDbo.builder().isEnabled(false).build();

      assertThat(dbo.isEnabled()).isFalse();
    }
  }
}
