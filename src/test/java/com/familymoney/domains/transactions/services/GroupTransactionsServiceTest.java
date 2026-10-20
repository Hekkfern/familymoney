package com.familymoney.domains.transactions.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.familymoney.domains.transactions.exceptions.ExpenseNotFoundException;
import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.PaymentNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.repositories.ExpenseRepository;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.PaymentRepository;
import com.familymoney.domains.transactions.repositories.TransactionRepository;
import com.familymoney.domains.transactions.repositories.entitites.ExpenseEntity;
import com.familymoney.domains.transactions.repositories.entitites.ExpensePaymentEntity;
import com.familymoney.domains.transactions.repositories.entitites.ExpenseShareEntity;
import com.familymoney.domains.transactions.repositories.entitites.FullExpenseEntity;
import com.familymoney.domains.transactions.repositories.entitites.GroupEntity;
import com.familymoney.domains.transactions.repositories.entitites.PaymentEntity;
import com.familymoney.domains.transactions.repositories.entitites.TransactionReference;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.services.data.TransactionType;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.utils.AuthorizedUser;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class GroupTransactionsServiceTest {

  private static final Instant DONE_AT = Instant.parse("2026-01-01T12:00:00Z");
  private final GroupId groupId = GroupId.generate();
  private final UserId sender = UserId.generate();
  private final UserId recipient = UserId.generate();
  private final AuthorizedUser requester = new AuthorizedUser(sender, Role.USER);
  @Mock private GroupRepository groupRepository;
  @Mock private TransactionRepository transactionRepository;
  @Mock private ExpenseRepository expenseRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private GroupChecker groupChecker;
  @InjectMocks private DefaultGroupService service;

  @Nested
  class GetGroupTransactions {

    @Test
    void hydrates_both_types_in_page_order_with_one_batch_per_type() {
      final UUID sharedId = UUID.randomUUID();
      final UUID otherExpenseId = UUID.randomUUID();
      final FullExpenseEntity firstExpense = expense(sharedId, DONE_AT);
      final FullExpenseEntity lastExpense = expense(otherExpenseId, DONE_AT.minusSeconds(2));
      final PaymentEntity payment = payment(sharedId, DONE_AT.minusSeconds(1));
      final PageRequest pageable = PageRequest.of(1, 3);
      when(transactionRepository.findAllByGroupId(groupId, pageable))
          .thenReturn(
              new PageImpl<>(
                  List.of(
                      reference(sharedId, TransactionType.EXPENSE, DONE_AT),
                      reference(sharedId, TransactionType.PAYMENT, payment.doneAt()),
                      reference(
                          otherExpenseId, TransactionType.EXPENSE, lastExpense.expense().doneAt())),
                  pageable,
                  10));
      stubCurrency();
      final Set<ExpenseId> expenseIds =
          Set.of(ExpenseId.fromUuid(sharedId), ExpenseId.fromUuid(otherExpenseId));
      when(expenseRepository.findAllByGroupIdAndIds(groupId, expenseIds))
          .thenReturn(List.of(lastExpense, firstExpense));
      when(paymentRepository.findAllByGroupIdAndIds(groupId, Set.of(payment.id())))
          .thenReturn(List.of(payment));

      final Page<TransactionData> result = service.getGroupTransactions(groupId, requester, 1, 3);

      assertThat(result.getNumber()).isEqualTo(1);
      assertThat(result.getSize()).isEqualTo(3);
      assertThat(result.getTotalElements()).isEqualTo(10);
      assertThat(result.getTotalPages()).isEqualTo(4);
      assertThat(result.getContent()).hasSize(3);
      final TransactionData.Expense first = (TransactionData.Expense) result.getContent().get(0);
      assertThat(first.expense().id()).isEqualTo(firstExpense.expense().id());
      assertThat(first.expense().totalAmount().getCurrency().getCurrencyCode()).isEqualTo("EUR");
      assertThat(first.expense().totalAmount().getNumber().numberValueExact(BigDecimal.class))
          .isEqualByComparingTo("12.345");
      assertThat(first.expense().doneAt()).isEqualTo(DONE_AT);
      assertThat(first.expense().shares()).containsEntry(recipient, new BigDecimal("12.345"));
      assertThat(first.expense().payers()).containsEntry(sender, new BigDecimal("12.345"));
      final TransactionData.Payment second = (TransactionData.Payment) result.getContent().get(1);
      assertThat(second.payment().id()).isEqualTo(payment.id());
      assertThat(second.payment().money().getCurrency().getCurrencyCode()).isEqualTo("EUR");
      assertThat(second.payment().money().getNumber().numberValueExact(BigDecimal.class))
          .isEqualByComparingTo("7.125");
      assertThat(second.payment().doneAt()).isEqualTo(payment.doneAt());
      assertThat(second.payment().from()).isEqualTo(sender);
      assertThat(second.payment().to()).isEqualTo(recipient);
      assertThat(((TransactionData.Expense) result.getContent().get(2)).expense().id())
          .isEqualTo(lastExpense.expense().id());
      verify(expenseRepository).findAllByGroupIdAndIds(groupId, expenseIds);
      verify(paymentRepository).findAllByGroupIdAndIds(groupId, Set.of(payment.id()));
      verify(groupRepository).findById(groupId);
      verify(groupChecker).checkIfGroupExists(groupId);
      verify(groupChecker).checkIfUserIsInGroup(requester.id(), groupId);
    }

    @Test
    void expense_only_page_skips_payment_loading() {
      final UUID id = UUID.randomUUID();
      stubPage(reference(id, TransactionType.EXPENSE, DONE_AT));
      stubCurrency();
      when(expenseRepository.findAllByGroupIdAndIds(groupId, Set.of(ExpenseId.fromUuid(id))))
          .thenReturn(List.of(expense(id, DONE_AT)));

      assertThat(service.getGroupTransactions(groupId, requester, 0, 20).getContent())
          .hasOnlyElementsOfType(TransactionData.Expense.class);
      verifyNoInteractions(paymentRepository);
    }

    @Test
    void payment_only_page_skips_expense_loading_and_admin_skips_membership_check() {
      final UUID id = UUID.randomUUID();
      stubPage(reference(id, TransactionType.PAYMENT, DONE_AT));
      stubCurrency();
      when(paymentRepository.findAllByGroupIdAndIds(groupId, Set.of(PaymentId.fromUuid(id))))
          .thenReturn(List.of(payment(id, DONE_AT)));

      assertThat(
              service
                  .getGroupTransactions(groupId, new AuthorizedUser(sender, Role.ADMIN), 0, 20)
                  .getContent())
          .hasOnlyElementsOfType(TransactionData.Payment.class);
      verifyNoInteractions(expenseRepository);
      verify(groupChecker, never()).checkIfUserIsInGroup(sender, groupId);
    }

    @Test
    void empty_out_of_range_page_preserves_total_without_loading_details_or_currency() {
      final PageRequest pageable = PageRequest.of(3, 20);
      when(transactionRepository.findAllByGroupId(groupId, pageable))
          .thenReturn(new PageImpl<>(List.of(), pageable, 22));

      final Page<TransactionData> result = service.getGroupTransactions(groupId, requester, 3, 20);

      assertThat(result.getContent()).isEmpty();
      assertThat(result.getNumber()).isEqualTo(3);
      assertThat(result.getTotalElements()).isEqualTo(22);
      verifyNoInteractions(expenseRepository, paymentRepository, groupRepository);
    }

    @Test
    void missing_expense_fails_instead_of_dropping_a_reference() {
      final UUID id = UUID.randomUUID();
      stubPage(reference(id, TransactionType.EXPENSE, DONE_AT));
      stubCurrency();
      when(expenseRepository.findAllByGroupIdAndIds(groupId, Set.of(ExpenseId.fromUuid(id))))
          .thenReturn(List.of());

      assertThatThrownBy(() -> service.getGroupTransactions(groupId, requester, 0, 20))
          .isInstanceOf(ExpenseNotFoundException.class)
          .hasMessageContaining(id.toString());
    }

    @Test
    void missing_payment_fails_instead_of_dropping_a_reference() {
      final UUID id = UUID.randomUUID();
      stubPage(reference(id, TransactionType.PAYMENT, DONE_AT));
      stubCurrency();
      when(paymentRepository.findAllByGroupIdAndIds(groupId, Set.of(PaymentId.fromUuid(id))))
          .thenReturn(List.of());

      assertThatThrownBy(() -> service.getGroupTransactions(groupId, requester, 0, 20))
          .isInstanceOf(PaymentNotFoundException.class)
          .hasMessageContaining(id.toString());
    }

    @Test
    void nonexistent_group_is_rejected_before_loading_transactions() {
      doThrow(new GroupNotFoundException("Missing group"))
          .when(groupChecker)
          .checkIfGroupExists(groupId);

      assertThatThrownBy(() -> service.getGroupTransactions(groupId, requester, 0, 20))
          .isInstanceOf(GroupNotFoundException.class);
      verifyNoInteractions(transactionRepository, expenseRepository, paymentRepository);
    }

    @Test
    void nonmember_is_rejected_before_loading_transactions() {
      doThrow(new UserIsNotMemberOfGroupException("Not a member"))
          .when(groupChecker)
          .checkIfUserIsInGroup(sender, groupId);

      assertThatThrownBy(() -> service.getGroupTransactions(groupId, requester, 0, 20))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
      verifyNoInteractions(transactionRepository, expenseRepository, paymentRepository);
    }
  }

  private void stubCurrency() {
    when(groupRepository.findById(groupId))
        .thenReturn(
            Optional.of(
                new GroupEntity(
                    groupId,
                    GroupName.fromString("Group"),
                    Description.of("Group"),
                    Currency.getInstance("EUR"))));
  }

  private void stubPage(final TransactionReference reference) {
    final PageRequest pageable = PageRequest.of(0, 20);
    when(transactionRepository.findAllByGroupId(groupId, pageable))
        .thenReturn(new PageImpl<>(List.of(reference), pageable, 1));
  }

  private TransactionReference reference(
      final UUID id, final TransactionType type, final Instant doneAt) {
    return new TransactionReference(id, type, doneAt);
  }

  private FullExpenseEntity expense(final UUID id, final Instant doneAt) {
    return new FullExpenseEntity(
        new ExpenseEntity(ExpenseId.fromUuid(id), Description.of("Dinner"), groupId, doneAt),
        List.of(new ExpenseShareEntity(recipient, new BigDecimal("12.345"))),
        List.of(new ExpensePaymentEntity(sender, new BigDecimal("12.345"))));
  }

  private PaymentEntity payment(final UUID id, final Instant doneAt) {
    return new PaymentEntity(
        PaymentId.fromUuid(id),
        Description.of("Settlement"),
        new BigDecimal("7.125"),
        sender,
        recipient,
        groupId,
        doneAt);
  }
}
