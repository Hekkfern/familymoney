package com.familymoney.familymoney.services;

import com.familymoney.familymoney.properties.AppProperties;
import com.familymoney.familymoney.properties.MailSenderProperties;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.UserName;
import lombok.RequiredArgsConstructor;
import lombok.val;
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
      Email toEmail, UserName username, EmailVerificationToken verificationToken) {
    val mimeMessage = mailSender.createMimeMessage();
    val mimeMessageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

    try {
      mimeMessageHelper.setFrom(
          String.format("%s <%s>", mailSenderProperties.name(), mailSenderProperties.email()));
      mimeMessageHelper.setTo(toEmail.value());
      mimeMessageHelper.setSubject("Email Verification");

      val context = new Context();
      context.setVariable("userName", username.value());
      context.setVariable("appName", appProperties.name());
      context.setVariable(
          "verificationUrl",
          String.format("https://miticketdecomida.com/verify-email/%s", verificationToken.value()));
      val VERIFICATION_EMAIL_TEMPLATE_NAME = "email-verification";
      val processedString = htmlTemplateEngine.process(VERIFICATION_EMAIL_TEMPLATE_NAME, context);
      mimeMessageHelper.setText(processedString, true);

      mailSender.send(mimeMessage);
    } catch (Exception e) {
      throw new RuntimeException("Failed to send email verification email");
    }
  }

  @Override
  public void sendPasswordResetEmail(
      Email toEmail, UserName username, EmailVerificationToken resetToken) {
    // TODO("Not yet implemented")
  }

  @Override
  public void sendSecurityAlertEmail(Email toEmail, UserName username) {
    val mimeMessage = mailSender.createMimeMessage();
    val mimeMessageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

    try {
      mimeMessageHelper.setFrom("${mailSenderProperties.name} <${mailSenderProperties.email}>");
      mimeMessageHelper.setTo(toEmail.value());
      mimeMessageHelper.setSubject("Security Alert");

      val context = new Context();
      context.setVariable("userName", username.value());
      context.setVariable("appName", appProperties.name());
      val SECURITY_ALERT_EMAIL_TEMPLATE_NAME = "email-securityalert";
      val processedString = htmlTemplateEngine.process(SECURITY_ALERT_EMAIL_TEMPLATE_NAME, context);
      mimeMessageHelper.setText(processedString, true);

      mailSender.send(mimeMessage);
    } catch (Exception e) {
      throw new RuntimeException("Failed to send security alert email");
    }
  }
}
