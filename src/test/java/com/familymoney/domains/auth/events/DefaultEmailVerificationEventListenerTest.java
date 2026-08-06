package com.familymoney.domains.auth.events;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.familymoney.domains.auth.services.IEmailSenderService;
import com.familymoney.domains.auth.types.EmailVerificationToken;
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
class DefaultEmailVerificationEventListenerTest {

  @Mock private IEmailSenderService emailSenderService;

  @InjectMocks private DefaultEmailVerificationEventListener listener;

  @Nested
  class OnEmailVerificationRequested {

    @Test
    void delegates_email_delivery_to_the_sender() {
      final EmailVerificationRequestedEvent event = event();

      listener.onEmailVerificationRequested(event);

      verify(emailSenderService)
          .sendEmailVerificationEmail(event.email(), event.username(), event.verificationToken());
    }

    @Test
    void contains_email_delivery_failures() {
      final EmailVerificationRequestedEvent event = event();
      doThrow(new RuntimeException("SMTP unavailable"))
          .when(emailSenderService)
          .sendEmailVerificationEmail(event.email(), event.username(), event.verificationToken());

      assertThatCode(() -> listener.onEmailVerificationRequested(event)).doesNotThrowAnyException();
    }
  }

  private EmailVerificationRequestedEvent event() {
    return new EmailVerificationRequestedEvent(
        UserId.generate(),
        Email.fromString(FakeGenerator.email()),
        UserName.fromString(FakeGenerator.username()),
        EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken()));
  }
}
