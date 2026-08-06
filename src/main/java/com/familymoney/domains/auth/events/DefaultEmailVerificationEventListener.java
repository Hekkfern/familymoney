package com.familymoney.domains.auth.events;

import com.familymoney.domains.auth.services.IEmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Delivers registration verification emails asynchronously after transaction commit. */
@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultEmailVerificationEventListener implements IEmailVerificationEventListener {

  private final IEmailSenderService emailSenderService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Override
  public void onEmailVerificationRequested(final EmailVerificationRequestedEvent event) {
    try {
      emailSenderService.sendEmailVerificationEmail(
          event.email(), event.username(), event.verificationToken());
    } catch (final RuntimeException ignored) {
      log.error(
          "Failed to deliver the registration email-verification message for user {}",
          event.userId());
    }
  }
}
