package com.familymoney.domains.transactions.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.familymoney.domains.transactions.exceptions.GroupInvitationInvalidException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupInvitationsReachedException;
import com.familymoney.domains.transactions.exceptions.TransactionGroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.TransactionNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.repositories.IBalanceRepository;
import com.familymoney.domains.transactions.repositories.IGroupInvitationRepository;
import com.familymoney.domains.transactions.repositories.IGroupRepository;
import com.familymoney.domains.transactions.repositories.ITransactionRepository;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupDto;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupInvitationDto;
import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.repositories.entitites.GroupEntity;
import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.repositories.entitites.TransactionEntity;
import com.familymoney.domains.transactions.repositories.entitites.UserGroupEntity;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.services.data.UpdateTransactionData;
import com.familymoney.domains.transactions.types.BalanceId;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpirationTime;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.transactions.types.TransactionId;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.repositories.IUserRepository;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.properties.GroupInvitationProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import lombok.val;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TransactionGroupServiceTest {

  private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");
  private static final CurrencyUnit CURRENCY_USD = Monetary.getCurrency("USD");

  @Mock private IGroupRepository groupRepository;
  @Mock private IBalanceRepository balanceRepository;
  @Mock private ITransactionRepository transactionRepository;
  @Mock private IGroupInvitationRepository groupInvitationRepository;
  @Mock private IUserRepository userRepository;
  @Spy private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
  @Mock private GroupInvitationProperties groupInvitationProperties;

  @InjectMocks private TransactionGroupService transactionGroupService;

  // Helpers to build DB objects
  private GroupEntity groupDbo(GroupId id) {
    return new GroupEntity(
        id, GroupName.fromString("group"), Description.of("desc"), CURRENCY_USD, NOW, NOW);
  }

  private TransactionEntity transactionDbo(TransactionId id, GroupId groupId) {
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

  private GroupInvitationEntity invitationDbo(
      GroupId groupId, GroupInvitationToken token, Instant expiresAt) {
    return new GroupInvitationEntity(
        UUID.randomUUID(), groupId, null, token, NOW, ExpirationTime.of(expiresAt));
  }

  private void mockCreateInGroupRepository() {
    when(groupRepository.create(any(CreateGroupDto.class)))
        .thenAnswer(
            invocation -> {
              CreateGroupDto dto = invocation.getArgument(0, CreateGroupDto.class);
              return Optional.of(
                  new GroupEntity(
                      dto.id(), // return the same UserId received
                      dto.name(),
                      dto.description(),
                      dto.currency(),
                      NOW,
                      NOW));
            });
  }

  private void mockAddUserToGroupRepository() {
    when(groupRepository.addUser(any(UserId.class), any(GroupId.class)))
        .thenAnswer(
            invocation -> {
              UserId userId = invocation.getArgument(0, UserId.class);
              GroupId groupId = invocation.getArgument(1, GroupId.class);
              return Optional.of(new UserGroupEntity(userId, groupId, NOW));
            });
  }

  private void mockCreateInGroupInvitationRepository() {
    when(groupInvitationRepository.create(any(CreateGroupInvitationDto.class)))
        .thenAnswer(
            invocation -> {
              CreateGroupInvitationDto dto =
                  invocation.getArgument(0, CreateGroupInvitationDto.class);
              return Optional.of(
                  new GroupInvitationEntity(
                      dto.id(), // return the same UserId received
                      dto.groupId(),
                      dto.userId(),
                      dto.token(),
                      NOW,
                      dto.expiresAt()));
            });
  }

  @Nested
  class CreateGroup {

    @Test
    void returns_id_when_repository_succeeds() {
      val groupName = GroupName.fromString("mygroup");
      val desc = Description.of("mydesc");
      val createdBy = UserId.generate();
      mockCreateInGroupRepository();
      mockAddUserToGroupRepository();

      assertThatCode(
              () -> transactionGroupService.createGroup(groupName, desc, CURRENCY_USD, createdBy))
          .doesNotThrowAnyException();

      verify(groupRepository).addUser(eq(createdBy), any(GroupId.class));
    }

    @Test
    void throws_when_repository_returns_empty() {
      when(groupRepository.create(any())).thenReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  transactionGroupService.createGroup(
                      GroupName.fromString("n"),
                      Description.of("d"),
                      CURRENCY_USD,
                      UserId.generate()))
          .isInstanceOf(DatabaseExecutionException.class)
          .hasMessageContaining("Unable to create group");
    }

    @Test
    void throws_when_addUser_returns_empty() {
      mockCreateInGroupRepository();
      when(groupRepository.addUser(any(UserId.class), any(GroupId.class)))
          .thenReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  transactionGroupService.createGroup(
                      GroupName.fromString("n"),
                      Description.of("d"),
                      CURRENCY_USD,
                      UserId.generate()))
          .isInstanceOf(DatabaseExecutionException.class)
          .hasMessageContaining("Unable to assign owner to the new group");
    }
  }

  @Nested
  class DeleteGroup {

    @Test
    void deletes_group_when_group_exists_and_user_is_member_of_the_group() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);

      transactionGroupService.deleteGroup(gid, user);

      verify(groupRepository).deleteById(gid);
    }

    @Test
    void throws_when_when_group_doesnt_exist() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(false);

      assertThatThrownBy(() -> transactionGroupService.deleteGroup(gid, user))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }

    @Test
    void throws_when_when_group_exists_and_user_is_not_member_of_the_group() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

      assertThatThrownBy(() -> transactionGroupService.deleteGroup(gid, user))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }
  }

  @Nested
  class GetGroupsByUser {

    @Test
    void get_page_from_repository() {
      val user = UserId.generate();
      val gid = GroupId.generate();
      val g = groupDbo(gid);
      Pageable p = PageRequest.of(0, 10);
      when(groupRepository.findByUserId(user, p)).thenReturn(new PageImpl<>(List.of(g)));

      val page = transactionGroupService.getGroupsByUser(user, p);

      assertThat(page.getContent()).hasSize(1);
      assertThat(page.getContent().get(0).id()).isEqualTo(gid);
    }
  }

  @Nested
  class GetGroupInfo {

    @Test
    void returns_data_when_group_exists_and_user_is_member_of_the_group() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);
      when(groupRepository.findById(gid)).thenReturn(Optional.of(groupDbo(gid)));

      val data = transactionGroupService.getGroupInfo(gid, user);

      assertThat(data).isNotNull();
      assertThat(data.id()).isEqualTo(gid);
    }

    @Test
    void throws_when_group_exists_and_user_is_not_member_of_the_group() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

      assertThatThrownBy(() -> transactionGroupService.getGroupInfo(gid, user))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }

    @Test
    void throws_when_group_doesnt_exist() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(false);

      assertThatThrownBy(() -> transactionGroupService.getGroupInfo(gid, user))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }
  }

  @Nested
  class UpdateGroupInfo {

    @Test
    void calls_repository_when_user_is_member_of_the_group() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);

      val data = new UpdateGroupData(null, Description.of("new"));
      transactionGroupService.updateGroupInfo(gid, user, data);

      verify(groupRepository).updateById(eq(gid), any());
    }

    @Test
    void throws_when_user_is_not_member_of_the_group() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

      val data = new UpdateGroupData(null, Description.of("new"));
      assertThatThrownBy(() -> transactionGroupService.updateGroupInfo(gid, user, data))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }

    @Test
    void throws_when_group_doesnt_exist() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(false);

      val data = new UpdateGroupData(null, Description.of("new"));
      assertThatThrownBy(() -> transactionGroupService.updateGroupInfo(gid, user, data))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }
  }

  @Nested
  class GetInvitationToken {

    @BeforeEach()
    void beforeEach() {
      lenient()
          .when(groupInvitationProperties.invitationDuration())
          .thenReturn(Duration.ofHours(1));
      lenient().when(groupInvitationProperties.maxNumInvitations()).thenReturn(5);
    }

    @Test
    void returns_token_when_created() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);
      mockCreateInGroupInvitationRepository();

      assertThatCode(() -> transactionGroupService.getInvitationToken(gid, user))
          .doesNotThrowAnyException();

      verify(groupInvitationRepository).create(any(CreateGroupInvitationDto.class));
    }

    @Test
    void throws_when_user_is_not_member_of_the_group() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

      assertThatThrownBy(() -> transactionGroupService.getInvitationToken(gid, user))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }

    @Test
    void throws_when_create_fails() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);
      when(groupInvitationRepository.create(any(CreateGroupInvitationDto.class)))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> transactionGroupService.getInvitationToken(gid, user))
          .isInstanceOf(DatabaseExecutionException.class);
    }

    @Test
    void throws_when_group_doesnt_exist() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(false);

      assertThatThrownBy(() -> transactionGroupService.getInvitationToken(gid, user))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }

    @Test
    void throws_when_maximum_number_of_invitations_reached() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);
      when(groupInvitationRepository.countByGroupIdAndUserId(gid, user)).thenReturn(20L);

      assertThatThrownBy(() -> transactionGroupService.getInvitationToken(gid, user))
          .isInstanceOf(MaximumGroupInvitationsReachedException.class);
    }
  }

  @Nested
  class EnterToGroupWithToken {

    @Test
    void adds_user_when_token_is_valid() {
      val gid = GroupId.generate();
      val token = GroupInvitationToken.generate();
      val invitation = invitationDbo(gid, token, NOW.plusSeconds(3600));
      val user = UserId.generate();
      when(groupInvitationRepository.findByToken(token)).thenReturn(Optional.of(invitation));

      transactionGroupService.enterToGroupWithToken(token, user);

      verify(groupInvitationRepository).deleteByToken(token);
      verify(groupRepository).addUser(user, gid);
    }

    @Test
    void throws_when_token_is_missing() {
      val token = GroupInvitationToken.generate();
      val user = UserId.generate();
      when(groupInvitationRepository.findByToken(token)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> transactionGroupService.enterToGroupWithToken(token, user))
          .isInstanceOf(GroupInvitationInvalidException.class);
    }

    @Test
    void throws_when_token_is_expired() {
      val gid = GroupId.generate();
      val token = GroupInvitationToken.generate();
      val invitation = invitationDbo(gid, token, NOW.minusSeconds(10));
      val user = UserId.generate();
      when(groupInvitationRepository.findByToken(token)).thenReturn(Optional.of(invitation));

      assertThatThrownBy(() -> transactionGroupService.enterToGroupWithToken(token, user))
          .isInstanceOf(GroupInvitationInvalidException.class)
          .hasMessageContaining("expired");
    }
  }

  @Nested
  class GetUsersInGroup {

    @Test
    void returns_list_when_group_exists_and_user_is_member_of_the_group() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      val other = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);
      when(groupRepository.findUserIdsByGroupId(gid)).thenReturn(List.of(user, other));

      val users = transactionGroupService.getUsersInGroup(gid, user);

      assertThat(users).containsExactly(user, other);
    }

    @Test
    void throws_when_group_exists_but_user_is_not_member_of_the_group() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

      assertThatThrownBy(() -> transactionGroupService.getUsersInGroup(gid, user))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }

    @Test
    void throws_when_group_doesnt_exist() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(false);

      assertThatThrownBy(() -> transactionGroupService.getUsersInGroup(gid, user))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }
  }

  @Nested
  class RemoveUserFromGroup {

    @Test
    void calls_delete_when_group_exists_and_user_is_member_of_the_group() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      val toRemove = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);
      when(userRepository.existsById(toRemove)).thenReturn(true);

      assertThatCode(() -> transactionGroupService.removeUserFromGroup(gid, user, toRemove))
          .doesNotThrowAnyException();

      // Service currently deletes using (user, groupId) per implementation
      verify(groupRepository).deleteUser(toRemove, gid);
    }

    @Test
    void throws_when_group_exists_and_user_is_not_member_of_the_group() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      val toRemove = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);
      when(userRepository.existsById(toRemove)).thenReturn(true);

      assertThatThrownBy(() -> transactionGroupService.removeUserFromGroup(gid, user, toRemove))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }

    @Test
    void throws_when_group_doesnt_exist() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      val toRemove = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(false);

      assertThatThrownBy(() -> transactionGroupService.removeUserFromGroup(gid, user, toRemove))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }

    @Test
    void throws_when_user_doesnt_exist() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      val toRemove = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(userRepository.existsById(toRemove)).thenReturn(false);

      assertThatThrownBy(() -> transactionGroupService.removeUserFromGroup(gid, user, toRemove))
          .isInstanceOf(UserNotFoundException.class);
    }
  }

  @Nested
  class GetAllGroupBalances {

    @Test
    void maps_balances_correctly_when_group_exists_and_user_is_member_of_the_group() {
      val gid = GroupId.generate();
      val userA = UserId.generate();
      val userB = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(userA, gid)).thenReturn(true);
      val b = balanceDbo(BalanceId.generate(), gid, userA, userB);
      when(balanceRepository.findByGroup(gid)).thenReturn(List.of(b));

      val map = transactionGroupService.getAllGroupBalances(gid, userA);

      assertThat(map).hasSize(1).containsKey(userB);
      assertThat(map.get(userB)).isEqualTo(b.money());
    }

    @Test
    void throws_when_group_exists_and_user_is_not_member_of_the_group() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

      assertThatThrownBy(() -> transactionGroupService.getAllGroupBalances(gid, user))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }

    @Test
    void throws_when_group_doesnt_exist() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(false);

      assertThatThrownBy(() -> transactionGroupService.getAllGroupBalances(gid, user))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }
  }

  @Nested
  class GetGroupTransactions {

    @Test
    void returns_mapped_page_when_user_is_member_of_the_group() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);
      val tx = transactionDbo(TransactionId.generate(), gid);
      Pageable p = PageRequest.of(0, 10);
      when(transactionRepository.findAllByGroupId(gid, p)).thenReturn(new PageImpl<>(List.of(tx)));

      val page = transactionGroupService.getGroupTransactions(gid, user, p);

      assertThat(page.getContent()).hasSize(1);
      assertThat(page.getContent().get(0).id()).isEqualTo(tx.id());
    }

    @Test
    void throws_when_user_is_not_member_of_the_group() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      Pageable p = PageRequest.of(0, 10);
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

      assertThatThrownBy(() -> transactionGroupService.getGroupTransactions(gid, user, p))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }

    @Test
    void throws_when_group_doesnt_exist() {
      val gid = GroupId.generate();
      val user = UserId.generate();
      Pageable p = PageRequest.of(0, 10);
      when(groupRepository.existsById(gid)).thenReturn(false);

      assertThatThrownBy(() -> transactionGroupService.getGroupTransactions(gid, user, p))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }
  }

  @Nested
  class CreateTransactionInGroup {

    @Test
    void calls_repository_when_user_is_member_of_the_group() {
      val gid = GroupId.generate();
      val creator = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(creator, gid)).thenReturn(true);

      transactionGroupService.createTransactionInGroup(
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
      val gid = GroupId.generate();
      val creator = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(true);
      when(groupRepository.isUserInGroup(creator, gid)).thenReturn(false);

      assertThatThrownBy(
              () ->
                  transactionGroupService.createTransactionInGroup(
                      gid,
                      Description.of("d"),
                      UserId.generate(),
                      UserId.generate(),
                      Money.of(1, CURRENCY_USD),
                      NOW,
                      creator))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }

    @Test
    void throws_when_group_doesnt_exist() {
      val gid = GroupId.generate();
      val creator = UserId.generate();
      when(groupRepository.existsById(gid)).thenReturn(false);

      assertThatThrownBy(
              () ->
                  transactionGroupService.createTransactionInGroup(
                      gid,
                      Description.of("d"),
                      UserId.generate(),
                      UserId.generate(),
                      Money.of(1, CURRENCY_USD),
                      NOW,
                      creator))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }
  }

  @Nested
  class UpdateTransaction {

    @Test
    void updates_when_transaction_exists_and_user_is_member_of_the_group() {
      val gid = GroupId.generate();
      val txId = TransactionId.generate();
      val tx = transactionDbo(txId, gid);
      val user = UserId.generate();
      when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);

      transactionGroupService.updateTransaction(
          user, txId, new UpdateTransactionData(Description.of("x"), null, null, null, null));

      verify(transactionRepository).updateById(eq(txId), any());
    }

    @Test
    void throws_when_transaction_is_missing() {
      val txId = TransactionId.generate();
      val user = UserId.generate();
      when(transactionRepository.findById(txId)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  transactionGroupService.updateTransaction(
                      user, txId, new UpdateTransactionData(null, null, null, null, null)))
          .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void throws_when_user_is_not_member_of_the_group() {
      val gid = GroupId.generate();
      val txId = TransactionId.generate();
      val tx = transactionDbo(txId, gid);
      val user = UserId.generate();
      when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

      val data = new UpdateTransactionData(null, null, null, null, null);
      assertThatThrownBy(() -> transactionGroupService.updateTransaction(user, txId, data))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }
  }

  @Nested
  class DeleteTransaction {

    @Test
    void deletes_when_transaction_exists_and_user_is_member_of_the_group() {
      val gid = GroupId.generate();
      val txId = TransactionId.generate();
      val tx = transactionDbo(txId, gid);
      val user = UserId.generate();
      when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);

      transactionGroupService.deleteTransaction(user, txId);

      verify(transactionRepository).deleteById(txId);
    }

    @Test
    void throws_when_transaction_is_missing() {
      val txId = TransactionId.generate();
      val user = UserId.generate();
      when(transactionRepository.findById(txId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> transactionGroupService.deleteTransaction(user, txId))
          .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void throws_when_user_is_not_member_of_the_group() {
      val gid = GroupId.generate();
      val txId = TransactionId.generate();
      val tx = transactionDbo(txId, gid);
      val user = UserId.generate();
      when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));
      when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

      assertThatThrownBy(() -> transactionGroupService.deleteTransaction(user, txId))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }
  }
}
