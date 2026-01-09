package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.controllers.dtos.grouptransaction.*;
import com.familymoney.familymoney.controllers.mappers.grouptransaction.*;
import com.familymoney.familymoney.services.ITransactionGroupService;
import com.familymoney.familymoney.types.*;
import com.familymoney.familymoney.utils.AuthenticationUtils;
import java.util.UUID;
import javax.money.Monetary;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionGroupController implements ITransactionGroupController {

  private final ITransactionGroupService transactionGroupService;
  private final CreateGroupResponseMapper createGroupResponseMapper;
  private final GetGroupResponseMapper getGroupResponseMapper;
  private final UpdateGroupRequestMapper updateGroupRequestMapper;
  private final GetInvitationTokenResponseMapper getInvitationTokenResponseMapper;
  private final GetUsersInGroupResponseMapper getUsersInGroupResponseMapper;
  private final GetGroupBalancesResponseMapper getGroupBalancesResponseMapper;
  private final GetGroupTransactionsResponseMapper getGroupTransactionsResponseMapper;

  @Override
  public CreateGroupResponseDto createGroup(CreateGroupRequestDto request) {
    // Get user ID from security context (validated)
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Create group
    val groupId =
        transactionGroupService.createGroup(
            GroupName.fromString(request.name()),
            request.description().trim(),
            Monetary.getCurrency(request.currencyCode()),
            userId);
    // Generate response
    return createGroupResponseMapper.toDto(groupId);
  }

  @Override
  public void deleteGroup(UUID groupId) {
    // Get user ID from security context (validated)
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Delete group
    transactionGroupService.deleteGroupOwnedBy(GroupId.fromUuid(groupId), userId);
  }

  @Override
  public GetGroupResponseDto getGroupInfo(UUID groupId) {
    // Get user ID from security context (validated)
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Get group info
    val groupData = transactionGroupService.getGroupInfoOwnedBy(GroupId.fromUuid(groupId), userId);
    // Generate response
    return getGroupResponseMapper.toDto(groupData);
  }

  @Override
  public void updateGroupInfo(UUID groupId, UpdateGroupRequestDto request) {
    // Get user ID from security context (validated)
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Update group info
    transactionGroupService.updateGroupInfoOwnedBy(
        GroupId.fromUuid(groupId), userId, updateGroupRequestMapper.fromDto(request));
  }

  @Override
  public GetInvitationTokenResponseDto getInvitationToken(UUID groupId) {
    // Get user ID from security context (validated)
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Get invitation token
    val token =
        transactionGroupService.getInvitationTokenOwnedBy(GroupId.fromUuid(groupId), userId);
    // Generate response
    return getInvitationTokenResponseMapper.toDto(token);
  }

  @Override
  public void enterToGroup(EnterGroupRequestDto request) {
    // Get user ID from security context (validated)
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Enter to group
    transactionGroupService.enterToGroupWithToken(
        GroupInvitationToken.fromString(request.token()), userId);
  }

  @Override
  public GetUsersInGroupResponseDto getUsersInGroup(UUID groupId) {
    // Get user ID from security context (validated)
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Get users in group
    val users = transactionGroupService.getUsersInGroupOwnedBy(GroupId.fromUuid(groupId), userId);
    // Generate response
    return getUsersInGroupResponseMapper.toDto(users);
  }

  @Override
  public void removeUserInGroup(UUID groupId, RemoveUserRequestDto request) {
    // Get user ID from security context (validated)
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Remove user from group
    transactionGroupService.removeUserFromGroup(
        GroupId.fromUuid(groupId), userId, UserId.fromUuid(request.userId()));
  }

  @Override
  public GetGroupBalancesResponseDto getGroupBalances(UUID groupId) {
    // Get user ID from security context (validated)
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Get balances
    val balances = transactionGroupService.getGroupBalances(GroupId.fromUuid(groupId), userId);
    // Generate response
    return getGroupBalancesResponseMapper.toDto(balances);
  }

  @Override
  public GetTransactionsResponseDto getGroupTransactions(UUID groupId, Pageable pageable) {
    // Get user ID from security context (validated)
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Get balances
    val transactions =
        transactionGroupService.getGroupTransactions(GroupId.fromUuid(groupId), userId);
    // Generate response
    return getGroupTransactionsResponseMapper.toDto(transactions);
  }

  @Override
  public void createTransaction(UUID groupId, CreateTransactionRequestDto request) {
    // Get user ID from security context (validated)
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Create transaction
    transactionGroupService.createTransactionInGroup(
        GroupId.fromUuid(groupId),
        userId,
        request.description(),
        request.from(),
        request.to(),
        request.amount(),
        request.doneAt());
  }

  @Override
  public void updateTransaction(
      UUID groupId, UUID transactionId, UpdateTransactionRequestDto request) {
    // Get user ID from security context (validated)
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Update transaction
    transactionGroupService.updateTransactionInGroup(
        GroupId.fromUuid(groupId),
        userId,
        data);
  }

  @Override
  public void deleteTransaction(UUID groupId, UUID transactionId) {
    // Get user ID from security context (validated)
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Delete transaction
    transactionGroupService.deleteTransactionInGroup(
        GroupId.fromUuid(groupId), userId, TransactionId.fromUuid(transactionId));
  }
}
