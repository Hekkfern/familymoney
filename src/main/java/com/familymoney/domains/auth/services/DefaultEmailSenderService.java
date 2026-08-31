package com.familymoney.domains.auth.services;

import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.properties.AppProperties;
import com.familymoney.properties.MailSenderProperties;
import com.familymoney.properties.ResetPasswordProperties;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class DefaultEmailSenderService implements EmailSenderService {

  private final JavaMailSender mailSender;
  private final SpringTemplateEngine htmlTemplateEngine;
  private final MailSenderProperties mailSenderProperties;
  private final AppProperties appProperties;
  private final ResetPasswordProperties resetPasswordProperties;

  @Override
  public void sendEmailVerificationEmail(
      final Email toEmail,
      final UserName username,
      final EmailVerificationToken verificationToken) {
    final MimeMessage mimeMessage = mailSender.createMimeMessage();
    final MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

    try {
      mimeMessageHelper.setFrom(
          String.format("%s <%s>", mailSenderProperties.name(), mailSenderProperties.email()));
      mimeMessageHelper.setTo(toEmail.value());
      mimeMessageHelper.setSubject("Email Verification");

      final Context context = new Context();
      context.setVariable("userName", username.value());
      context.setVariable("appName", appProperties.name());
      context.setVariable(
          "verificationUrl",
          String.format("https://miticketdecomida.com/verify-email/%s", verificationToken.value()));
      final String VERIFICATION_EMAIL_TEMPLATE_NAME = "email-verification";
      final String processedString =
          htmlTemplateEngine.process(VERIFICATION_EMAIL_TEMPLATE_NAME, context);
      mimeMessageHelper.setText(processedString, true);

      mailSender.send(mimeMessage);
    } catch (final Exception exception) {
      throw new RuntimeException("Failed to send email verification email");
    }
  }

  @Override
  public void sendPasswordResetEmail(
      final Email toEmail, final UserName username, final PasswordResetToken resetToken) {
    final MimeMessage mimeMessage = mailSender.createMimeMessage();
    final MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

    try {
      mimeMessageHelper.setFrom(
          String.format("%s <%s>", mailSenderProperties.name(), mailSenderProperties.email()));
      mimeMessageHelper.setTo(toEmail.value());
      mimeMessageHelper.setSubject("Password Reset");

      final Context context = new Context();
      context.setVariable("userName", username.value());
      context.setVariable("appName", appProperties.name());
      context.setVariable(
          "resetUrl",
          UriComponentsBuilder.fromUri(resetPasswordProperties.resetUrl())
              .queryParam("token", resetToken.value())
              .build()
              .toUriString());
      context.setVariable(
          "tokenDurationMinutes", resetPasswordProperties.tokenDuration().toMinutes());
      final String resetEmailTemplateName = "email-passwordreset";
      final String processedString = htmlTemplateEngine.process(resetEmailTemplateName, context);
      mimeMessageHelper.setText(processedString, true);

      mailSender.send(mimeMessage);
    } catch (final Exception exception) {
      throw new RuntimeException("Failed to send password reset email", exception);
    }
  }
}
