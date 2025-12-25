package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.controllers.dtos.group.*;
import com.familymoney.familymoney.services.ITransactionGroupService;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.TransactionId;
import com.familymoney.familymoney.utils.AuthenticationUtils;
import javax.money.Monetary;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionGroupController implements ITransactionGroupController {

  private final ITransactionGroupService transactionGroupService;

  @Override
  public CreateGroupResponseDto createGroup(CreateGroupRequestDto request) {
    // Get user ID from security context (validated)
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Create group
    val groupId =
        transactionGroupService.createGroup(
            request.name(),
            request.description().trim(),
            Monetary.getCurrency(request.currencyCode()),
            userId);
    // Generate response
    return CreateGroupResponseDto.builder().id(groupId).build();
  }

  @Override
  public void deleteGroup(GroupId groupId) {
    // Get user ID from security context (validated)
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Delete group
    transactionGroupService.deleteGroupOwnedBy(groupId, userId);
  }

  @Override
  public GetGroupResponseDto getGroupInfo(GroupId groupId) {
    return null;
  }

  @Override
  public void updateGroupInfo(GroupId groupId, UpdateGroupRequestDto request) {}

  @Override
  public GetInvitationTokenResponseDto getInvitationToken(GroupId groupId) {
    return null;
  }

  @Override
  public void enterToGroup(GroupId groupId, EnterGroupRequestDto request) {}

  @Override
  public GetUsersInGroupResponseDto getUsersInGroup(GroupId groupId) {
    return null;
  }

  @Override
  public GetGroupBalancesResponseDto getGroupBalances(GroupId groupId) {
    return null;
  }

  @Override
  public GetTransactionsResponseDto getTransactions(GroupId groupId, Pageable pageable) {
    return null;
  }

  @Override
  public void createTransaction(GroupId groupId) {}

  @Override
  public void updateTransaction(
      GroupId groupId, TransactionId transactionId, UpdateTransactionRequestDto request) {}

  @Override
  public void deleteTransaction(GroupId groupId, TransactionId transactionId) {}
}
