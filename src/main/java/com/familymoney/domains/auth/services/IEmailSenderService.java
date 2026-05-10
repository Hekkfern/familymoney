package com.familymoney.domains.auth.services;

import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.UserName;

public interface IEmailSenderService {

  void sendEmailVerificationEmail(
      Email toEmail, UserName username, EmailVerificationToken verificationToken);

  void sendPasswordResetEmail(Email toEmail, UserName username, EmailVerificationToken resetToken);

  void sendSecurityAlertEmail(Email toEmail, UserName username);
}
