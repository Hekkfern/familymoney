package com.familymoney.domains.transactions.controllers;

import com.familymoney.domains.transactions.controllers.dtos.BalanceDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.EnterGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.GroupDto;
import com.familymoney.domains.transactions.controllers.dtos.InvitationTokenDto;
import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.mappers.CreateGroupResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.GetGroupBalancesResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.GetInvitationTokenResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.GetUsersInGroupResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.GroupDtoMapper;
import com.familymoney.domains.transactions.controllers.mappers.UpdateGroupRequestMapper;
import com.familymoney.domains.transactions.services.GroupService;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.utils.AuthenticationUtils;
import com.familymoney.utils.AuthorizedUser;
import com.familymoney.utils.PageResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.money.Monetary;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultGroupController implements GroupController {

  private final GroupService groupService;

  @Override
  public CreateGroupResponseDto createGroup(final CreateGroupRequestDto request) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Create group
    final GroupId groupId =
        groupService.createGroupAndAddCreatorAsMember(
            GroupName.fromString(request.name()),
            Description.of(request.description().trim()),
            Monetary.getCurrency(request.currencyCode()),
            user.id());
    // Generate response
    return CreateGroupResponseMapper.toDto(groupId);
  }

  @Override
  public List<GroupId> getGroupsForUser() {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Get groups of user
    final Page<GroupData> groupPages = groupService.getGroupsByUser(user.id(), pageable);
    // Generate response
    return new GetGroupsForUserResponseDto(
        PageResponse.from(groupPages.getContent().stream().map(GroupDtoMapper::toDto).toList()));
  }

  @Override
  public void deleteGroup(UUID groupId) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Delete group
    groupService.deleteGroup(GroupId.fromUuid(groupId), user.id());
  }

  @Override
  public GroupDto getGroupInfo(UUID groupId) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Get group info
    final GroupData groupData = groupService.getGroupInfo(GroupId.fromUuid(groupId), user.id());
    // Generate response
    return GroupDtoMapper.toDto(groupData);
  }

  @Override
  public void updateGroupInfo(UUID groupId, UpdateGroupRequestDto request) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Update group info
    groupService.updateGroupInfo(
        GroupId.fromUuid(groupId), user.id(), UpdateGroupRequestMapper.fromDto(request));
  }

  @Override
  public InvitationTokenDto getInvitationToken(UUID groupId) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Get invitation token
    final GroupInvitationToken token =
        groupService.getInvitationToken(GroupId.fromUuid(groupId), user.id());
    // Generate response
    return GetInvitationTokenResponseMapper.toDto(token);
  }

  @Override
  public void enterToGroup(EnterGroupRequestDto request) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Enter to group
    groupService.enterToGroupWithToken(GroupInvitationToken.fromString(request.token()), user.id());
  }

  @Override
  public List<UUID> getUsersInGroup(UUID groupId) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Get users in group
    final List<UserId> users = groupService.getUsersInGroup(GroupId.fromUuid(groupId), user.id());
    // Generate response
    return GetUsersInGroupResponseMapper.toDto(users);
  }

  @Override
  public void removeUserFromGroup(UUID groupId, UUID userId) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    // Remove user from group
    groupService.removeUserFromGroup(GroupId.fromUuid(groupId), user.id(), UserId.fromUuid(userId));
  }

  @Override
  public BalanceDto getGroupBalances(final UUID groupId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final Map<UserId, Money> balances =
        transactionService.getAllGroupBalances(GroupId.fromUuid(groupId), user.id());
    return GetGroupBalancesResponseMapper.toDto(balances);
  }

  @Override
  public PageResponse<TransactionDto> getGroupTransactions(UUID groupId, int page, int size) {
    return null;
  }
}
