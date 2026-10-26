package com.familymoney.domains.auth.services;

import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserName;

public interface EmailSenderService {

  /**
   * Sends an email-verification email containing the supplied raw token.
   *
   * @param toEmail recipient email address
   * @param username recipient username
   * @param verificationToken raw email-verification token
   */
  void sendEmailVerificationEmail(
      Email toEmail, UserName username, EmailVerificationToken verificationToken);

  /**
   * Sends a password-reset email containing the supplied raw single-use token.
   *
   * @param toEmail recipient email address
   * @param username recipient username
   * @param resetToken raw password reset token
   */
  void sendPasswordResetEmail(Email toEmail, UserName username, PasswordResetToken resetToken);

  /**
   * Sends a group invitation email containing the supplied raw single-use token.
   *
   * @param toEmail recipient email address
   * @param username recipient username
   * @param groupName name of the group the user is being invited to
   * @param invitationToken raw group invitation token
   */
  void sendGroupInvitationEmail(
      Email toEmail, UserName username, GroupName groupName, GroupInvitationToken invitationToken);
}
