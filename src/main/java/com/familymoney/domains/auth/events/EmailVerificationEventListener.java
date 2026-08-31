package com.familymoney.domains.auth.events;

/** Listener contract for email-verification delivery events. */
public interface EmailVerificationEventListener {

  /**
   * Delivers the email requested by a committed registration event.
   *
   * @param event email-verification delivery request.
   */
  void onEmailVerificationRequested(EmailVerificationRequestedEvent event);
}
