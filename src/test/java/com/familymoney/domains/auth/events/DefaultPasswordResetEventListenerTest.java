package com.familymoney.domains.auth.events;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.familymoney.domains.auth.services.IEmailSenderService;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.testutils.FakeGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultPasswordResetEventListenerTest {

  @Mock private IEmailSenderService emailSenderService;

  @InjectMocks private DefaultPasswordResetEventListener listener;

  @Nested
  class OnPasswordResetRequested {

    @Test
    void delegates_email_delivery_to_the_sender() {
      final PasswordResetRequestedEvent event = event();

      listener.onPasswordResetRequested(event);

      verify(emailSenderService)
          .sendPasswordResetEmail(event.email(), event.username(), event.resetToken());
    }

    @Test
    void contains_email_delivery_failures() {
      final PasswordResetRequestedEvent event = event();
      doThrow(new RuntimeException("SMTP unavailable"))
          .when(emailSenderService)
          .sendPasswordResetEmail(event.email(), event.username(), event.resetToken());

      assertThatCode(() -> listener.onPasswordResetRequested(event)).doesNotThrowAnyException();
    }
  }

  private PasswordResetRequestedEvent event() {
    return new PasswordResetRequestedEvent(
        UserId.generate(),
        Email.fromString(FakeGenerator.email()),
        UserName.fromString(FakeGenerator.username()),
        PasswordResetToken.generate());
  }
}
