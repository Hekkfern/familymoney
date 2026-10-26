package com.familymoney.domains.auth.services;

import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
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

  private static final String FROM_TEMPLATE = "%s <%s>";
  private static final String GROUP_INVITATION_EMAIL_TEMPLATE_NAME = "email-group-invitation";
  private static final String EMAIL_VERIFICATION_EMAIL_TEMPLATE_NAME = "email-verification";
  private static final String PASSWORD_RESET_EMAIL_TEMPLATE_NAME = "email-passwordreset";

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
        FROM_TEMPLATE.formatted(mailSenderProperties.name(), mailSenderProperties.email()));
      mimeMessageHelper.setTo(toEmail.value());
      mimeMessageHelper.setSubject("Email Verification");

      final Context context = new Context();
      context.setVariable("userName", username.value());
      context.setVariable("appName", appProperties.name());
      context.setVariable(
          "verificationUrl",
          "https://miticketdecomida.com/verify-email/%s".formatted(verificationToken.value()));
      final String processedString =
          htmlTemplateEngine.process(EMAIL_VERIFICATION_EMAIL_TEMPLATE_NAME, context);
      mimeMessageHelper.setText(processedString, true);

      mailSender.send(mimeMessage);
    } catch (final Exception _) {
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
        FROM_TEMPLATE.formatted(mailSenderProperties.name(), mailSenderProperties.email()));
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
      final String processedString =
          htmlTemplateEngine.process(PASSWORD_RESET_EMAIL_TEMPLATE_NAME, context);
      mimeMessageHelper.setText(processedString, true);

      mailSender.send(mimeMessage);
    } catch (final Exception exception) {
      throw new RuntimeException("Failed to send password reset email", exception);
    }
  }

  @Override
  public void sendGroupInvitationEmail(
      final Email toEmail,
      final UserName username,
      final GroupName groupName,
      final GroupInvitationToken invitationToken) {
    final MimeMessage mimeMessage = mailSender.createMimeMessage();
    final MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

    try {
      mimeMessageHelper.setFrom(
          FROM_TEMPLATE.formatted(mailSenderProperties.name(), mailSenderProperties.email()));
      mimeMessageHelper.setTo(toEmail.value());
      mimeMessageHelper.setSubject("Group Invitation");

      final Context context = new Context();
      context.setVariable("userName", username.value());
      context.setVariable("appName", appProperties.name());
      context.setVariable("groupName", groupName.value());
      context.setVariable("invitationToken", invitationToken.value());
      final String processedString =
          htmlTemplateEngine.process(GROUP_INVITATION_EMAIL_TEMPLATE_NAME, context);
      mimeMessageHelper.setText(processedString, true);

      mailSender.send(mimeMessage);
    } catch (final Exception exception) {
      throw new RuntimeException("Failed to send group invitation email", exception);
    }
  }
}
