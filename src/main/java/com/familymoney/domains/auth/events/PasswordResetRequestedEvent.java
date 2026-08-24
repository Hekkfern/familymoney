package com.familymoney.domains.auth.events;

import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;

/**
 * Event requesting delivery of a password-reset message after a committed transaction.
 *
 * @param userId identifier of the user requesting the reset
 * @param email destination email address
 * @param username username used in the email content
 * @param resetToken raw token included in the reset link
 */
public record PasswordResetRequestedEvent(
    UserId userId, Email email, UserName username, PasswordResetToken resetToken) {}
