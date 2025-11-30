package com.familymoney.familymoney.services;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.Username;
import org.springframework.lang.NonNull;

public interface IEmailSenderService {

  void sendEmailVerificationEmail(
      @NonNull Email toEmail,
      @NonNull Username username,
      @NonNull EmailVerificationToken verificationToken);

  void sendPasswordResetEmail(
      @NonNull Email toEmail,
      @NonNull Username username,
      @NonNull EmailVerificationToken resetToken);

  void sendSecurityAlertEmail(@NonNull Email toEmail, @NonNull Username username);
}
