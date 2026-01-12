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
public class GroupController implements IGroupController {

  private final ITransactionGroupService transactionGroupService;
  private final CreateGroupResponseMapper createGroupResponseMapper;
  private final GetGroupResponseMapper getGroupResponseMapper;
  private final UpdateGroupRequestMapper updateGroupRequestMapper;
  private final GetInvitationTokenResponseMapper getInvitationTokenResponseMapper;
  private final GetUsersInGroupResponseMapper getUsersInGroupResponseMapper;
  private final GetGroupBalancesResponseMapper getGroupBalancesResponseMapper;

  @Override
  public CreateGroupResponseDto createGroup(CreateGroupRequestDto request) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Create group
    val groupId =
        transactionGroupService.createGroup(
            GroupName.fromString(request.name()),
            request.description().trim(),
            Monetary.getCurrency(request.currencyCode()),
            user.id());
    // Generate response
    return createGroupResponseMapper.toDto(groupId);
  }

    @Override
    public GetGroupsResponseDto getGroupsOfUser(Pageable pageable) {
        // Get user ID from security context (validated)
        val user = AuthenticationUtils.getUserIdFromSecurityContext();
        // Get groups of user
        val groupPages = transactionGroupService.getGroups(user, pageable);
        // Generate response
        return GetGroupsResponseDto.builder()
                .groups(groupPages.getContent().stream().map(getGroupResponseMapper::toDto).toList())
                .build();
    }

    @Override
  public void deleteGroup(UUID groupId) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Delete group
    transactionGroupService.deleteGroup(GroupId.fromUuid(groupId), user);
  }

  @Override
  public GetGroupResponseDto getGroupInfo(UUID groupId) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Get group info
    val groupData = transactionGroupService.getGroupInfo(GroupId.fromUuid(groupId), user);
    // Generate response
    return getGroupResponseMapper.toDto(groupData);
  }

  @Override
  public void updateGroupInfo(UUID groupId, UpdateGroupRequestDto request) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Update group info
    transactionGroupService.updateGroupInfo(
        GroupId.fromUuid(groupId), user, updateGroupRequestMapper.fromDto(request));
  }

  @Override
  public GetInvitationTokenResponseDto getInvitationToken(UUID groupId) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Get invitation token
    val token = transactionGroupService.getInvitationToken(GroupId.fromUuid(groupId), user);
    // Generate response
    return getInvitationTokenResponseMapper.toDto(token);
  }

  @Override
  public void enterToGroup(EnterGroupRequestDto request) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Enter to group
    transactionGroupService.enterToGroupWithToken(
        GroupInvitationToken.fromString(request.token()), user);
  }

  @Override
  public GetUsersInGroupResponseDto getUsersInGroup(UUID groupId) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Get users in group
    val users = transactionGroupService.getUsersInGroup(GroupId.fromUuid(groupId), user);
    // Generate response
    return getUsersInGroupResponseMapper.toDto(users);
  }

  @Override
  public void removeUserInGroup(UUID groupId, RemoveUserRequestDto request) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Remove user from group
    transactionGroupService.removeUserFromGroup(
        GroupId.fromUuid(groupId), user, UserId.fromUuid(request.userId()));
  }

  @Override
  public GetGroupBalancesResponseDto getGroupBalances(UUID groupId) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Get balances
    val balances = transactionGroupService.getGroupBalances(GroupId.fromUuid(groupId), user);
    // Generate response
    return getGroupBalancesResponseMapper.toDto(balances);
  }
}
