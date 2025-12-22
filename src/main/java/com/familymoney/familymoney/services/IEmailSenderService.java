package com.familymoney.familymoney.services;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.UserName;

public interface IEmailSenderService {

  void sendEmailVerificationEmail(
          Email toEmail, UserName username, EmailVerificationToken verificationToken);

  void sendPasswordResetEmail(Email toEmail, UserName username, EmailVerificationToken resetToken);

  void sendSecurityAlertEmail(Email toEmail, UserName username);
}
