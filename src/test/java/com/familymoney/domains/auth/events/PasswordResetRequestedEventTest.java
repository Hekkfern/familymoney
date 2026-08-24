package com.familymoney.domains.auth.events;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.testutils.FakeGenerator;
import org.junit.jupiter.api.Test;

class PasswordResetRequestedEventTest {

  @Test
  void preserves_email_delivery_data() {
    final UserId userId = UserId.generate();
    final Email email = Email.fromString(FakeGenerator.email());
    final UserName username = UserName.fromString(FakeGenerator.username());
    final PasswordResetToken resetToken = PasswordResetToken.generate();

    final PasswordResetRequestedEvent event =
        new PasswordResetRequestedEvent(userId, email, username, resetToken);

    assertThat(event.userId()).isEqualTo(userId);
    assertThat(event.email()).isEqualTo(email);
    assertThat(event.username()).isEqualTo(username);
    assertThat(event.resetToken()).isEqualTo(resetToken);
  }
}
