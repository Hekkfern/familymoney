package com.familymoney.domains.auth.services;

import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.properties.AppProperties;
import com.familymoney.properties.MailSenderProperties;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailSenderService implements IEmailSenderService {

  private final JavaMailSender mailSender;
  private final SpringTemplateEngine htmlTemplateEngine;
  private final MailSenderProperties mailSenderProperties;
  private final AppProperties appProperties;

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
    } catch (Exception e) {
      throw new RuntimeException("Failed to send email verification email");
    }
  }

  @Override
  public void sendPasswordResetEmail(
      final Email toEmail, final UserName username, final EmailVerificationToken resetToken) {
    // TODO("Not yet implemented")
  }
}
