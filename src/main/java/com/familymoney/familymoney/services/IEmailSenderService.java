package com.familymoney.familymoney.services;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.Username;

public interface IEmailSenderService {

  void sendEmailVerificationEmail(
      Email toEmail, Username username, EmailVerificationToken verificationToken);

  void sendPasswordResetEmail(Email toEmail, Username username, EmailVerificationToken resetToken);

  void sendSecurityAlertEmail(Email toEmail, Username username);
}
