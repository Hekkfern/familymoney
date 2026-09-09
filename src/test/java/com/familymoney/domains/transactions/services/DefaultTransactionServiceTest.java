package com.familymoney.domains.transactions.services;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.familymoney.domains.transactions.exceptions.TransactionGroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.TransactionNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.repositories.BalanceRepository;
import com.familymoney.domains.transactions.repositories.TransactionRepository;
import com.familymoney.domains.transactions.repositories.dtos.UpdateTransactionDto;
import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.repositories.entitites.TransactionEntity;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.services.data.UpdateTransactionData;
import com.familymoney.domains.transactions.types.BalanceId;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.TransactionId;
import com.familymoney.domains.users.types.UserId;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DefaultTransactionServiceTest {

  private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");
  private static final CurrencyUnit CURRENCY_USD = Monetary.getCurrency("USD");
  @Spy private final Clock clock = Clock.fixed(NOW, DEFAULT_TIMEZONE_OFFSET);

  @Mock private GroupOperations groupOperations;
  @Mock private BalanceRepository balanceRepository;
  @Mock private TransactionRepository transactionRepository;

  @InjectMocks private DefaultTransactionService transactionService;

  private TransactionEntity transactionDbo(final TransactionId id, final GroupId groupId) {
    return new TransactionEntity(
        id,
        Description.of("tx-desc"),
        groupId,
        Money.of(10, CURRENCY_USD),
        UserId.generate(),
        UserId.generate(),
        NOW,
        NOW,
        NOW);
  }

  private BalanceEntity balanceDbo(BalanceId id, GroupId groupId, UserId u1, UserId u2) {
    return new BalanceEntity(id, groupId, Money.of(5, CURRENCY_USD), u1, u2);
  }

  @Nested
  class GetAllGroupBalances {

    @Test
    void maps_balances_correctly_when_group_exists_and_user_is_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId userA = UserId.generate();
      final UserId userB = UserId.generate();
      final BalanceEntity b = balanceDbo(BalanceId.generate(), gid, userA, userB);
      when(balanceRepository.findByGroup(gid)).thenReturn(List.of(b));

      final Map<UserId, Money> map = transactionService.getAllGroupBalances(gid, userA);

      assertThat(map).hasSize(1).containsKey(userB);
      assertThat(map.get(userB)).isEqualTo(b.money());
    }

    @Test
    void throws_when_group_exists_and_user_is_not_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      doThrow(new UserIsNotMemberOfGroupException("User is not a member of the group"))
          .when(groupOperations)
          .checkIfUserIsInGroup(user, gid);

      assertThatThrownBy(() -> transactionService.getAllGroupBalances(gid, user))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }

    @Test
    void throws_when_group_doesnt_exist() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      doThrow(new TransactionGroupNotFoundException("Group not found"))
          .when(groupOperations)
          .checkIfGroupExists(gid);

      assertThatThrownBy(() -> transactionService.getAllGroupBalances(gid, user))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }
  }

  @Nested
  class GetGroupTransactions {

    @Test
    void returns_mapped_page_when_user_is_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      final TransactionEntity tx = transactionDbo(TransactionId.generate(), gid);
      Pageable p = PageRequest.of(0, 10);
      when(transactionRepository.findAllByGroupId(gid, p)).thenReturn(new PageImpl<>(List.of(tx)));

      final Page<TransactionData> page = transactionService.getGroupTransactions(gid, user, p);

      assertThat(page.getContent()).hasSize(1);
      assertThat(page.getContent().get(0).id()).isEqualTo(tx.id());
    }

    @Test
    void throws_when_user_is_not_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      Pageable p = PageRequest.of(0, 10);
      doThrow(new UserIsNotMemberOfGroupException("User is not a member of the group"))
          .when(groupOperations)
          .checkIfUserIsInGroup(user, gid);

      assertThatThrownBy(() -> transactionService.getGroupTransactions(gid, user, p))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }

    @Test
    void throws_when_group_doesnt_exist() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      Pageable p = PageRequest.of(0, 10);
      doThrow(new TransactionGroupNotFoundException("Group not found"))
          .when(groupOperations)
          .checkIfGroupExists(gid);

      assertThatThrownBy(() -> transactionService.getGroupTransactions(gid, user, p))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }
  }

  @Nested
  class CreateTransactionInGroup {

    @Test
    void calls_repository_when_user_is_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId creator = UserId.generate();

      transactionService.createTransactionInGroup(
          gid,
          Description.of("d"),
          UserId.generate(),
          UserId.generate(),
          Money.of(1, CURRENCY_USD),
          NOW,
          creator);

      verify(transactionRepository).create(any());
    }

    @Test
    void throws_when_creator_is_not_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId creator = UserId.generate();
      doThrow(new UserIsNotMemberOfGroupException("User is not a member of the group"))
          .when(groupOperations)
          .checkIfUserIsInGroup(creator, gid);

      final Description description = Description.of("d");
      final UserId user1 = UserId.generate();
      final UserId user2 = UserId.generate();
      final Money amount = Money.of(1, CURRENCY_USD);
      assertThatThrownBy(
              () ->
                  transactionService.createTransactionInGroup(
                      gid, description, user1, user2, amount, NOW, creator))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }

    @Test
    void throws_when_group_doesnt_exist() {
      final GroupId gid = GroupId.generate();
      final UserId creator = UserId.generate();
      doThrow(new TransactionGroupNotFoundException("Group not found"))
          .when(groupOperations)
          .checkIfGroupExists(gid);

      final Description description = Description.of("d");
      final UserId user1 = UserId.generate();
      final UserId user2 = UserId.generate();
      final Money amount = Money.of(1, CURRENCY_USD);
      assertThatThrownBy(
              () ->
                  transactionService.createTransactionInGroup(
                      gid, description, user1, user2, amount, NOW, creator))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }
  }

  @Nested
  class UpdateTransaction {

    @Test
    void updates_all_fields() {
      final GroupId gid = GroupId.generate();
      final TransactionId txId = TransactionId.generate();
      final TransactionEntity tx = transactionDbo(txId, gid);
      final UserId user1 = UserId.generate();
      final UserId user2 = UserId.generate();
      final Description description = Description.of("new description");
      final Money amount = Money.of(12, CURRENCY_USD);
      final Instant doneAt = NOW.plusSeconds(300);
      when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));

      transactionService.updateTransaction(
          user1,
          txId,
          UpdateTransactionData.builder()
              .description(description)
              .amount(amount)
              .to(user1)
              .from(user2)
              .doneAt(doneAt)
              .build());

      verify(transactionRepository)
          .updateById(txId, new UpdateTransactionDto(amount, description, user2, user1, doneAt));
    }

    @Test
    void updates_description_only() {
      final GroupId gid = GroupId.generate();
      final TransactionId txId = TransactionId.generate();
      final TransactionEntity tx = transactionDbo(txId, gid);
      final UserId user = UserId.generate();
      final Description description = Description.of("new description");
      when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));

      transactionService.updateTransaction(
          user, txId, UpdateTransactionData.builder().description(description).build());

      verify(transactionRepository)
          .updateById(txId, new UpdateTransactionDto(null, description, null, null, null));
    }

    @Test
    void throws_when_transaction_is_missing() {
      final TransactionId txId = TransactionId.generate();
      final UserId user = UserId.generate();
      when(transactionRepository.findById(txId)).thenReturn(Optional.empty());

      final UpdateTransactionData data = UpdateTransactionData.builder().build();
      assertThatThrownBy(() -> transactionService.updateTransaction(user, txId, data))
          .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void throws_when_user_is_not_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final TransactionId txId = TransactionId.generate();
      final TransactionEntity tx = transactionDbo(txId, gid);
      final UserId user = UserId.generate();
      when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));
      doThrow(new UserIsNotMemberOfGroupException("User is not a member of the group"))
          .when(groupOperations)
          .checkIfUserIsInGroup(user, gid);

      final UpdateTransactionData data =
          UpdateTransactionData.builder().doneAt(NOW.plusSeconds(3600)).build();
      assertThatThrownBy(() -> transactionService.updateTransaction(user, txId, data))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }
  }

  @Nested
  class DeleteTransaction {

    @Test
    void deletes_when_transaction_exists_and_user_is_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final TransactionId txId = TransactionId.generate();
      final TransactionEntity tx = transactionDbo(txId, gid);
      final UserId user = UserId.generate();
      when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));

      transactionService.deleteTransaction(user, txId);

      verify(transactionRepository).deleteById(txId);
    }

    @Test
    void throws_when_transaction_is_missing() {
      final TransactionId txId = TransactionId.generate();
      final UserId user = UserId.generate();
      when(transactionRepository.findById(txId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> transactionService.deleteTransaction(user, txId))
          .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void throws_when_user_is_not_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final TransactionId txId = TransactionId.generate();
      final TransactionEntity tx = transactionDbo(txId, gid);
      final UserId user = UserId.generate();
      when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));
      doThrow(new UserIsNotMemberOfGroupException("User is not a member of the group"))
          .when(groupOperations)
          .checkIfUserIsInGroup(user, gid);

      assertThatThrownBy(() -> transactionService.deleteTransaction(user, txId))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }
  }
}
