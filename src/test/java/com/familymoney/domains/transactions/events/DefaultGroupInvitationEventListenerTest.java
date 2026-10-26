package com.familymoney.domains.transactions.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.familymoney.domains.auth.services.EmailSenderService;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class DefaultGroupInvitationEventListenerTest {

  @Mock private EmailSenderService emailSenderService;
  @InjectMocks private DefaultGroupInvitationEventListener listener;

  @Nested
  class OnGroupInvitationRequested {

    @Test
    void delegates_email_delivery_to_the_sender() {
      final GroupInvitationRequestedEvent event = event();

      listener.onGroupInvitationRequested(event);

      verify(emailSenderService)
          .sendGroupInvitationEmail(
              event.email(), event.username(), event.groupName(), event.invitationToken());
    }

    @Test
    void contains_email_delivery_errors() {
      final GroupInvitationRequestedEvent event = event();
      doThrow(new RuntimeException("SMTP unavailable"))
          .when(emailSenderService)
          .sendGroupInvitationEmail(
              event.email(), event.username(), event.groupName(), event.invitationToken());

      assertThatCode(() -> listener.onGroupInvitationRequested(event)).doesNotThrowAnyException();
    }

    @Test
    void sends_asynchronously_only_after_commit() {
      final CompletableFuture<Thread> deliveryThread = new CompletableFuture<>();
      doAnswer(
              invocation -> {
                deliveryThread.complete(Thread.currentThread());
                return null;
              })
          .when(emailSenderService)
          .sendGroupInvitationEmail(any(), any(), any(), any());

      try (final AnnotationConfigApplicationContext context = context()) {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
          context.publishEvent(event());
          verifyNoInteractions(emailSenderService);

          TransactionSynchronizationManager.getSynchronizations()
              .forEach(
                  synchronization ->
                      synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));

          assertThat(deliveryThread.orTimeout(5, TimeUnit.SECONDS).join())
              .isNotSameAs(Thread.currentThread());
        } finally {
          TransactionSynchronizationManager.clear();
        }
      }
    }

    @Test
    void does_not_send_when_the_transaction_rolls_back() {
      try (final AnnotationConfigApplicationContext context = context()) {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
          context.publishEvent(event());

          TransactionSynchronizationManager.getSynchronizations()
              .forEach(
                  synchronization ->
                      synchronization.afterCompletion(
                          TransactionSynchronization.STATUS_ROLLED_BACK));

          verifyNoInteractions(emailSenderService);
        } finally {
          TransactionSynchronizationManager.clear();
        }
      }
    }
  }

  private AnnotationConfigApplicationContext context() {
    final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(AsyncConfiguration.class);
    context.registerBean(TransactionalEventListenerFactory.class);
    context.registerBean(EmailSenderService.class, () -> emailSenderService);
    context.registerBean(DefaultGroupInvitationEventListener.class);
    context.refresh();
    return context;
  }

  private GroupInvitationRequestedEvent event() {
    return new GroupInvitationRequestedEvent(
        UserId.generate(),
        Email.fromString("invitee@example.com"),
        UserName.fromString("Invitee"),
        GroupName.fromString("Family"),
        GroupInvitationToken.generate());
  }

  @Configuration(proxyBeanMethods = false)
  @EnableAsync
  static class AsyncConfiguration {}
}
