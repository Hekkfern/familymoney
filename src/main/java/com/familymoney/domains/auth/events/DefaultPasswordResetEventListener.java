package com.familymoney.domains.auth.events;

import com.familymoney.domains.auth.services.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Delivers password-reset emails asynchronously after the requesting transaction commits. */
@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultPasswordResetEventListener implements PasswordResetEventListener {

  private final EmailSenderService emailSenderService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Override
  public void onPasswordResetRequested(final PasswordResetRequestedEvent event) {
    try {
      emailSenderService.sendPasswordResetEmail(
          event.email(), event.username(), event.resetToken());
    } catch (final RuntimeException exception) {
      log.error("Failed to deliver the password-reset message for user {}", event.userId());
    }
  }
}
