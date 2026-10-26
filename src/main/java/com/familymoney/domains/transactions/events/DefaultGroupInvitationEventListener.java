package com.familymoney.domains.transactions.events;

import com.familymoney.domains.auth.services.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Sends group invitation emails asynchronously after the transaction commits. */
@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultGroupInvitationEventListener implements GroupInvitationEventListener {

  private final EmailSenderService emailSenderService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Override
  public void onGroupInvitationRequested(final GroupInvitationRequestedEvent event) {
    try {
      emailSenderService.sendGroupInvitationEmail(
          event.email(), event.username(), event.groupName(), event.invitationToken());
    } catch (final RuntimeException _) {
      log.error("Failed to deliver the group invitation email for user {}", event.userId());
    }
  }
}
