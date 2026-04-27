package com.familymoney.services;

import com.familymoney.types.Email;
import com.familymoney.types.EmailVerificationToken;
import com.familymoney.types.UserName;

public interface IEmailSenderService {

  void sendEmailVerificationEmail(
      Email toEmail, UserName username, EmailVerificationToken verificationToken);

  void sendPasswordResetEmail(Email toEmail, UserName username, EmailVerificationToken resetToken);

  void sendSecurityAlertEmail(Email toEmail, UserName username);
}
