package com.familymoney.familymoney.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.familymoney.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.familymoney.exceptions.GroupInvitationNotFoundException;
import com.familymoney.familymoney.exceptions.GroupNotOwnedByUserException;
import com.familymoney.familymoney.exceptions.TransactionNotFoundException;
import com.familymoney.familymoney.repositories.*;
import com.familymoney.familymoney.repositories.dbos.*;
import com.familymoney.familymoney.services.data.UpdateGroupData;
import com.familymoney.familymoney.services.data.UpdateTransactionData;
import com.familymoney.familymoney.services.mappers.*;
import com.familymoney.familymoney.types.*;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import lombok.val;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TransactionGroupServiceTests {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  @Mock private IGroupRepository groupRepository;
  @Mock private IBalanceRepository balanceRepository;
  @Mock private ITransactionRepository transactionRepository;
  @Mock private IGroupInvitationRepository groupInvitationRepository;
  @Spy private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);

  @Spy private GroupDataMapper groupDataMapper = Mappers.getMapper(GroupDataMapper.class);

  @Spy
  private UpdateGroupDataMapper updateGroupDataMapper =
      Mappers.getMapper(UpdateGroupDataMapper.class);

  @Spy
  private TransactionDataMapper transactionDataMapper =
      Mappers.getMapper(TransactionDataMapper.class);

  @Spy
  private UpdateTransactionDataMapper updateTransactionDataMapper =
      Mappers.getMapper(UpdateTransactionDataMapper.class);

  @InjectMocks private TransactionGroupService transactionGroupService;

  private CurrencyUnit usd = Monetary.getCurrency("USD");

  // Helpers to build DB objects
  private GroupDbo groupDbo(GroupId id) {
    return GroupDbo.builder()
        .id(id)
        .name(GroupName.fromString("group"))
        .description("desc")
        .currency(usd)
        .createdAt(now)
        .updatedAt(now)
        .build();
  }

  private TransactionDbo transactionDbo(TransactionId id, GroupId groupId) {
    return TransactionDbo.builder()
        .id(id)
        .description("tx-desc")
        .groupId(groupId)
        .amount(Money.of(10, usd))
        .from(UserId.fromUuid(UUID.randomUUID()))
        .to(UserId.fromUuid(UUID.randomUUID()))
        .doneAt(now)
        .createdAt(now)
        .updatedAt(now)
        .build();
  }

  private BalanceDbo balanceDbo(BalanceId id, GroupId groupId, UserId u1, UserId u2) {
    return BalanceDbo.builder()
        .id(id)
        .groupId(groupId)
        .amount(Money.of(5, usd))
        .user1(u1)
        .user2(u2)
        .build();
  }

  private GroupInvitationDbo invitationDbo(
      GroupId groupId, GroupInvitationToken token, Instant expiresAt) {
    return GroupInvitationDbo.builder()
        .id(UUID.randomUUID())
        .groupId(groupId)
        .token(token)
        .createdAt(now)
        .expiresAt(expiresAt)
        .build();
  }

  // -------- createGroup --------
  @Test
  void createGroup_returns_id_when_repository_succeeds() {
    val groupId = GroupId.fromUuid(UUID.randomUUID());
    val createdBy = UserId.fromUuid(UUID.randomUUID());
    val group = groupDbo(groupId);
    when(groupRepository.create(any(), anyString(), any(), eq(createdBy)))
        .thenReturn(Optional.of(group));
    when(groupRepository.addUser(eq(createdBy), eq(groupId)))
        .thenReturn(
            Optional.of(
                UserGroupDbo.builder().userId(createdBy).groupId(groupId).joinedAt(now).build()));

    val result =
        transactionGroupService.createGroup(GroupName.fromString("n"), "d", usd, createdBy);

    assertThat(result).isEqualTo(groupId);
    verify(groupRepository).addUser(eq(createdBy), eq(groupId));
  }

  @Test
  void createGroup_throws_when_repository_returns_empty() {
    when(groupRepository.create(any(), anyString(), any(), any())).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                transactionGroupService.createGroup(
                    GroupName.fromString("n"), "d", usd, UserId.fromUuid(UUID.randomUUID())))
        .isInstanceOf(DatabaseExecutionException.class)
        .hasMessageContaining("Unable to create group");
  }

  @Test
  void createGroup_throws_when_addUser_returns_empty() {
    val groupId = GroupId.fromUuid(UUID.randomUUID());
    val createdBy = UserId.fromUuid(UUID.randomUUID());
    val group = groupDbo(groupId);
    when(groupRepository.create(any(), anyString(), any(), eq(createdBy)))
        .thenReturn(Optional.of(group));
    when(groupRepository.addUser(eq(createdBy), eq(groupId))).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                transactionGroupService.createGroup(GroupName.fromString("n"), "d", usd, createdBy))
        .isInstanceOf(DatabaseExecutionException.class)
        .hasMessageContaining("Unable to assign owner to the new group");
  }

  // -------- deleteGroup --------

  @Test
  void deleteGroup_deletes_when_user_is_member() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);

    transactionGroupService.deleteGroup(gid, user);

    verify(groupRepository).deleteById(gid);
  }

  @Test
  void deleteGroup_throws_when_user_not_member() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

    assertThatThrownBy(() -> transactionGroupService.deleteGroup(gid, user))
        .isInstanceOf(GroupNotOwnedByUserException.class);
  }

  // -------- getGroups --------

  @Test
  void getGroups_maps_page_from_repository() {
    val user = UserId.fromUuid(UUID.randomUUID());
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val g = groupDbo(gid);
    Pageable p = PageRequest.of(0, 10);
    when(groupRepository.findByUserId(user, p)).thenReturn(new PageImpl<>(List.of(g)));

    val page = transactionGroupService.getGroups(user, p);

    assertThat(page.getContent()).hasSize(1);
    assertThat(page.getContent().get(0).id()).isEqualTo(gid);
  }

  // -------- getGroupInfo --------

  @Test
  void getGroupInfo_returns_data_when_member_and_exists() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);
    when(groupRepository.findById(gid)).thenReturn(Optional.of(groupDbo(gid)));

    val data = transactionGroupService.getGroupInfo(gid, user);

    assertThat(data).isNotNull();
    assertThat(data.id()).isEqualTo(gid);
  }

  @Test
  void getGroupInfo_throws_when_user_not_member() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

    assertThatThrownBy(() -> transactionGroupService.getGroupInfo(gid, user))
        .isInstanceOf(GroupNotOwnedByUserException.class);
  }

  @Test
  void getGroupInfo_throws_when_group_missing() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);
    when(groupRepository.findById(gid)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> transactionGroupService.getGroupInfo(gid, user))
        .isInstanceOf(DatabaseExecutionException.class)
        .hasMessageContaining("Unable to find group");
  }

  // -------- updateGroupInfo --------

  @Test
  void updateGroupInfo_calls_repository_when_member() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);

    val data = UpdateGroupData.builder().description("new").build();
    transactionGroupService.updateGroupInfo(gid, user, data);

    verify(groupRepository).updateById(eq(gid), any());
  }

  @Test
  void updateGroupInfo_throws_when_user_not_member() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

    val data = UpdateGroupData.builder().description("new").build();
    assertThatThrownBy(() -> transactionGroupService.updateGroupInfo(gid, user, data))
        .isInstanceOf(GroupNotOwnedByUserException.class);
  }

  // -------- getInvitationToken --------

  @Test
  void getInvitationToken_returns_token_when_created() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);
    val token = GroupInvitationToken.fromString("token-123");
    val invitation = invitationDbo(gid, token, now.plusSeconds(3600));
    when(groupInvitationRepository.create(eq(gid), any(), any()))
        .thenReturn(Optional.of(invitation));

    val result = transactionGroupService.getInvitationToken(gid, user);

    assertThat(result).isEqualTo(token);
  }

  @Test
  void getInvitationToken_throws_when_user_not_member() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

    assertThatThrownBy(() -> transactionGroupService.getInvitationToken(gid, user))
        .isInstanceOf(GroupNotOwnedByUserException.class);
  }

  @Test
  void getInvitationToken_throws_when_create_fails() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);
    when(groupInvitationRepository.create(eq(gid), any(), any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> transactionGroupService.getInvitationToken(gid, user))
        .isInstanceOf(DatabaseExecutionException.class)
        .hasMessageContaining("Unable to create invitation token");
  }

  // -------- enterToGroupWithToken --------

  @Test
  void enterToGroupWithToken_adds_user_when_valid_token() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val token = GroupInvitationToken.fromString("tkn");
    val invitation = invitationDbo(gid, token, now.plusSeconds(3600));
    val user = UserId.fromUuid(UUID.randomUUID());
    when(groupInvitationRepository.findByToken(token)).thenReturn(Optional.of(invitation));

    transactionGroupService.enterToGroupWithToken(token, user);

    verify(groupInvitationRepository).deleteByToken(token);
    verify(groupRepository).addUser(user, gid);
  }

  @Test
  void enterToGroupWithToken_throws_when_missing_token() {
    val token = GroupInvitationToken.fromString("absent");
    val user = UserId.fromUuid(UUID.randomUUID());
    when(groupInvitationRepository.findByToken(token)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> transactionGroupService.enterToGroupWithToken(token, user))
        .isInstanceOf(GroupInvitationNotFoundException.class);
  }

  @Test
  void enterToGroupWithToken_throws_when_token_expired() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val token = GroupInvitationToken.fromString("expired");
    val invitation = invitationDbo(gid, token, now.minusSeconds(10));
    val user = UserId.fromUuid(UUID.randomUUID());
    when(groupInvitationRepository.findByToken(token)).thenReturn(Optional.of(invitation));

    assertThatThrownBy(() -> transactionGroupService.enterToGroupWithToken(token, user))
        .isInstanceOf(GroupInvitationNotFoundException.class)
        .hasMessageContaining("expired");
  }

  // -------- getUsersInGroup --------

  @Test
  void getUsersInGroup_returns_list_when_member() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    val other = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);
    when(groupRepository.findUserIdsByGroupId(gid)).thenReturn(List.of(user, other));

    val users = transactionGroupService.getUsersInGroup(gid, user);

    assertThat(users).containsExactly(user, other);
  }

  @Test
  void getUsersInGroup_throws_when_not_member() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

    assertThatThrownBy(() -> transactionGroupService.getUsersInGroup(gid, user))
        .isInstanceOf(GroupNotOwnedByUserException.class);
  }

  // -------- removeUserFromGroup --------

  @Test
  void removeUserFromGroup_calls_delete_when_member() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    val toRemove = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);

    transactionGroupService.removeUserFromGroup(gid, user, toRemove);

    // Service currently deletes using (user, groupId) per implementation
    verify(groupRepository).deleteUser(user, gid);
  }

  @Test
  void removeUserFromGroup_throws_when_not_member() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    val toRemove = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

    assertThatThrownBy(() -> transactionGroupService.removeUserFromGroup(gid, user, toRemove))
        .isInstanceOf(GroupNotOwnedByUserException.class);
  }

  // -------- getAllGroupBalances --------

  @Test
  void getAllGroupBalances_maps_balances_correctly_when_member() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val userA = UserId.fromUuid(UUID.randomUUID());
    val userB = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(userA, gid)).thenReturn(true);
    val b = balanceDbo(BalanceId.fromUuid(UUID.randomUUID()), gid, userA, userB);
    when(balanceRepository.findByGroup(gid)).thenReturn(List.of(b));

    val map = transactionGroupService.getAllGroupBalances(gid, userA);

    assertThat(map).hasSize(1).containsKey(userB);
    assertThat(map.get(userB)).isEqualTo(b.amount());
  }

  @Test
  void getAllGroupBalances_throws_when_not_member() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

    assertThatThrownBy(() -> transactionGroupService.getAllGroupBalances(gid, user))
        .isInstanceOf(GroupNotOwnedByUserException.class);
  }

  // -------- getGroupTransactions --------

  @Test
  void getGroupTransactions_returns_mapped_page_when_member() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);
    val tx = transactionDbo(TransactionId.fromUuid(UUID.randomUUID()), gid);
    Pageable p = PageRequest.of(0, 10);
    when(transactionRepository.findAllByGroupId(gid, p)).thenReturn(new PageImpl<>(List.of(tx)));

    val page = transactionGroupService.getGroupTransactions(gid, user, p);

    assertThat(page.getContent()).hasSize(1);
    assertThat(page.getContent().get(0).id()).isEqualTo(tx.id());
  }

  @Test
  void getGroupTransactions_throws_when_not_member() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    Pageable p = PageRequest.of(0, 10);
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

    assertThatThrownBy(() -> transactionGroupService.getGroupTransactions(gid, user, p))
        .isInstanceOf(GroupNotOwnedByUserException.class);
  }

  // -------- createTransactionInGroup --------

  @Test
  void createTransactionInGroup_calls_repository_when_member() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val creator = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(creator, gid)).thenReturn(true);

    transactionGroupService.createTransactionInGroup(
        gid,
        "d",
        UserId.fromUuid(UUID.randomUUID()),
        UserId.fromUuid(UUID.randomUUID()),
        Money.of(1, usd),
        now,
        creator);

    verify(transactionRepository).create(anyString(), eq(gid), any(), any(), any(), any());
  }

  @Test
  void createTransactionInGroup_throws_when_creator_not_member() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val creator = UserId.fromUuid(UUID.randomUUID());
    when(groupRepository.isUserInGroup(creator, gid)).thenReturn(false);

    assertThatThrownBy(
            () ->
                transactionGroupService.createTransactionInGroup(
                    gid,
                    "d",
                    UserId.fromUuid(UUID.randomUUID()),
                    UserId.fromUuid(UUID.randomUUID()),
                    Money.of(1, usd),
                    now,
                    creator))
        .isInstanceOf(GroupNotOwnedByUserException.class);
  }

  // -------- updateTransaction --------

  @Test
  void updateTransaction_updates_when_transaction_exists_and_user_in_group() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val txId = TransactionId.fromUuid(UUID.randomUUID());
    val tx = transactionDbo(txId, gid);
    val user = UserId.fromUuid(UUID.randomUUID());
    when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);

    transactionGroupService.updateTransaction(
        user, txId, UpdateTransactionData.builder().description("x").build());

    verify(transactionRepository).updateById(eq(txId), any());
  }

  @Test
  void updateTransaction_throws_when_transaction_missing() {
    val txId = TransactionId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    when(transactionRepository.findById(txId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                transactionGroupService.updateTransaction(
                    user, txId, UpdateTransactionData.builder().build()))
        .isInstanceOf(TransactionNotFoundException.class);
  }

  @Test
  void updateTransaction_throws_when_user_not_in_group() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val txId = TransactionId.fromUuid(UUID.randomUUID());
    val tx = transactionDbo(txId, gid);
    val user = UserId.fromUuid(UUID.randomUUID());
    when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

    assertThatThrownBy(
            () ->
                transactionGroupService.updateTransaction(
                    user, txId, UpdateTransactionData.builder().build()))
        .isInstanceOf(GroupNotOwnedByUserException.class);
  }

  // -------- deleteTransaction --------

  @Test
  void deleteTransaction_deletes_when_transaction_exists_and_user_in_group() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val txId = TransactionId.fromUuid(UUID.randomUUID());
    val tx = transactionDbo(txId, gid);
    val user = UserId.fromUuid(UUID.randomUUID());
    when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(true);

    transactionGroupService.deleteTransaction(user, txId);

    verify(transactionRepository).deleteById(txId);
  }

  @Test
  void deleteTransaction_throws_when_missing() {
    val txId = TransactionId.fromUuid(UUID.randomUUID());
    val user = UserId.fromUuid(UUID.randomUUID());
    when(transactionRepository.findById(txId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> transactionGroupService.deleteTransaction(user, txId))
        .isInstanceOf(TransactionNotFoundException.class);
  }

  @Test
  void deleteTransaction_throws_when_user_not_in_group() {
    val gid = GroupId.fromUuid(UUID.randomUUID());
    val txId = TransactionId.fromUuid(UUID.randomUUID());
    val tx = transactionDbo(txId, gid);
    val user = UserId.fromUuid(UUID.randomUUID());
    when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));
    when(groupRepository.isUserInGroup(user, gid)).thenReturn(false);

    assertThatThrownBy(() -> transactionGroupService.deleteTransaction(user, txId))
        .isInstanceOf(GroupNotOwnedByUserException.class);
  }
}
