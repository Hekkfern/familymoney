package com.familymoney.domains.transactions.controllers;

import com.familymoney.domains.transactions.controllers.dtos.CreateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.EnterGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupBalancesResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupsResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetInvitationTokenResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetUsersInGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.RemoveUserInGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.mappers.CreateGroupResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.GetGroupBalancesResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.GetGroupResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.GetInvitationTokenResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.GetUsersInGroupResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.UpdateGroupRequestMapper;
import com.familymoney.domains.transactions.services.TransactionGroupService;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.utils.AuthenticationUtils;
import com.familymoney.utils.AuthorizedUser;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.money.Monetary;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultGroupController implements GroupController {

  private final TransactionGroupService transactionGroupService;

  @Override
  public CreateGroupResponseDto createGroup(final CreateGroupRequestDto request) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Create group
    final GroupId groupId =
        transactionGroupService.createGroup(
            GroupName.fromString(request.name()),
            Description.of(request.description().trim()),
            Monetary.getCurrency(request.currencyCode()),
            user.id());
    // Generate response
    return CreateGroupResponseMapper.toDto(groupId);
  }

  @Override
  public GetGroupsResponseDto getGroupsOfUser(final Pageable pageable) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Get groups of user
    final Page<GroupData> groupPages = transactionGroupService.getGroupsByUser(user.id(), pageable);
    // Generate response
    return GetGroupsResponseDto.builder()
        .groups(groupPages.getContent().stream().map(GetGroupResponseMapper::toDto).toList())
        .build();
  }

  @Override
  public void deleteGroup(UUID groupId) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Delete group
    transactionGroupService.deleteGroup(GroupId.fromUuid(groupId), user.id());
  }

  @Override
  public GetGroupResponseDto getGroupInfo(UUID groupId) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Get group info
    final GroupData groupData =
        transactionGroupService.getGroupInfo(GroupId.fromUuid(groupId), user.id());
    // Generate response
    return GetGroupResponseMapper.toDto(groupData);
  }

  @Override
  public void updateGroupInfo(UUID groupId, UpdateGroupRequestDto request) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Update group info
    transactionGroupService.updateGroupInfo(
        GroupId.fromUuid(groupId), user.id(), UpdateGroupRequestMapper.fromDto(request));
  }

  @Override
  public GetInvitationTokenResponseDto getInvitationToken(UUID groupId) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Get invitation token
    final GroupInvitationToken token =
        transactionGroupService.getInvitationToken(GroupId.fromUuid(groupId), user.id());
    // Generate response
    return GetInvitationTokenResponseMapper.toDto(token);
  }

  @Override
  public void enterToGroup(EnterGroupRequestDto request) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Enter to group
    transactionGroupService.enterToGroupWithToken(
        GroupInvitationToken.fromString(request.token()), user.id());
  }

  @Override
  public GetUsersInGroupResponseDto getUsersInGroup(UUID groupId) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Get users in group
    final List<UserId> users =
        transactionGroupService.getUsersInGroup(GroupId.fromUuid(groupId), user.id());
    // Generate response
    return GetUsersInGroupResponseMapper.toDto(users);
  }

  @Override
  public void removeUserInGroup(UUID groupId, RemoveUserInGroupRequestDto request) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Remove user from group
    transactionGroupService.removeUserFromGroup(
        GroupId.fromUuid(groupId), user.id(), UserId.fromUuid(request.userId()));
  }

  @Override
  public GetGroupBalancesResponseDto getGroupBalances(UUID groupId) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Get balances
    final Map<UserId, Money> balances =
        transactionGroupService.getAllGroupBalances(GroupId.fromUuid(groupId), user.id());
    // Generate response
    return GetGroupBalancesResponseMapper.toDto(balances);
  }
}
