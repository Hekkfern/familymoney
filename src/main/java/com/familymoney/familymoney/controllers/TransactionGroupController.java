package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.controllers.dtos.group.*;
import com.familymoney.familymoney.controllers.mappers.CreateGroupResponseMapper;
import com.familymoney.familymoney.services.ITransactionGroupService;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupName;
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
    return null;
  }

  @Override
  public void updateGroupInfo(UUID groupId, UpdateGroupRequestDto request) {}

  @Override
  public GetInvitationTokenResponseDto getInvitationToken(UUID groupId) {
    return null;
  }

  @Override
  public void enterToGroup(UUID groupId, EnterGroupRequestDto request) {}

  @Override
  public GetUsersInGroupResponseDto getUsersInGroup(UUID groupId) {
    return null;
  }

  @Override
  public GetGroupBalancesResponseDto getGroupBalances(UUID groupId) {
    return null;
  }

  @Override
  public GetTransactionsResponseDto getTransactions(UUID groupId, Pageable pageable) {
    return null;
  }

  @Override
  public void createTransaction(UUID groupId) {}

  @Override
  public void updateTransaction(
      UUID groupId, UUID transactionId, UpdateTransactionRequestDto request) {}

  @Override
  public void deleteTransaction(UUID groupId, UUID transactionId) {}
}
