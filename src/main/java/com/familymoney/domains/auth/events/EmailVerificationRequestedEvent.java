package com.familymoney.domains.auth.events;

import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;

/**
 * Event requesting delivery of an email-verification message after user registration commits.
 *
 * @param userId identifier of the registered user.
 * @param email destination email address.
 * @param username registered username.
 * @param verificationToken raw token included in the verification link.
 */
public record EmailVerificationRequestedEvent(
    UserId userId, Email email, UserName username, EmailVerificationToken verificationToken) {}
