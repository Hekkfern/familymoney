package com.familymoney.domains.auth.events;

/** Listener contract for password-reset email delivery events. */
public interface PasswordResetEventListener {

  /**
   * Delivers the email requested by a committed password-reset event.
   *
   * @param event password-reset email delivery request
   */
  void onPasswordResetRequested(PasswordResetRequestedEvent event);
}
