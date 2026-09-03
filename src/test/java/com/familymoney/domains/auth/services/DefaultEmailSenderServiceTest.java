package com.familymoney.domains.auth.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.familymoney.config.ThymeleafConfig;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.properties.AppProperties;
import com.familymoney.properties.MailSenderProperties;
import com.familymoney.properties.ResetPasswordProperties;
import com.familymoney.testutils.FakeGenerator;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;

@ExtendWith(MockitoExtension.class)
class DefaultEmailSenderServiceTest {

  @Mock private JavaMailSender javaMailSender;

  @Spy private SpringTemplateEngine htmlTemplateEngine = new ThymeleafConfig().templateEngine();

  @Spy
  private MailSenderProperties mailSenderProperties =
      MailSenderProperties.builder().name("John Doe").email("test@example.com").build();

  @Spy private AppProperties appProperties = AppProperties.builder().name("testapp").build();

  @Spy
  private ResetPasswordProperties resetPasswordProperties =
      new ResetPasswordProperties(
          Duration.ofHours(1),
          Duration.ofMinutes(2),
          URI.create("https://example.com/reset-password"));

  @InjectMocks private DefaultEmailSenderService emailSenderService;

  @BeforeEach
  void setup() {
    when(javaMailSender.createMimeMessage())
        .thenReturn(new MimeMessage((jakarta.mail.Session) null));
  }

  @Test
  void sendEmailVerificationEmail_sends_email_successfully() {
    final Email email = Email.fromString(FakeGenerator.email());
    final UserName username = UserName.fromString(FakeGenerator.username());
    final EmailVerificationToken token = EmailVerificationToken.generate();

    final ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);

    emailSenderService.sendEmailVerificationEmail(email, username, token);

    verify(javaMailSender).send(messageCaptor.capture());

    final MimeMessage sentMessage = messageCaptor.getValue();
    assertNotNull(sentMessage);
    assertThatCode(
            () ->
                assertEquals(
                    String.format(
                        "%s <%s>", mailSenderProperties.name(), mailSenderProperties.email()),
                    sentMessage.getFrom()[0].toString()))
        .doesNotThrowAnyException();
    assertThatCode(
            () ->
                assertEquals(
                    email.toString(),
                    sentMessage.getRecipients(Message.RecipientType.TO)[0].toString()))
        .doesNotThrowAnyException();
    assertThatCode(() -> assertFalse(sentMessage.getContent().toString().isEmpty()))
        .doesNotThrowAnyException();
  }

  @Test
  void sendPasswordResetEmail_sends_a_tokenized_reset_link() throws Exception {
    final Email email = Email.fromString(FakeGenerator.email());
    final UserName username = UserName.fromString(FakeGenerator.username());
    final PasswordResetToken token = PasswordResetToken.generate();
    final ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);

    emailSenderService.sendPasswordResetEmail(email, username, token);

    verify(javaMailSender).send(messageCaptor.capture());
    final MimeMessage sentMessage = messageCaptor.getValue();
    assertThat(sentMessage.getSubject()).isEqualTo("Password Reset");
    assertThat(sentMessage.getContent().toString())
        .contains("https://example.com/reset-password?token=" + token.value())
        .contains("60 minutes");
  }
}
