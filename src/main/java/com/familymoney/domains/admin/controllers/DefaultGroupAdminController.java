package com.familymoney.domains.admin.controllers;

import com.familymoney.domains.transactions.controllers.dtos.BalanceDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GroupDto;
import com.familymoney.domains.transactions.controllers.dtos.MemberDto;
import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.mappers.BalanceDtoMapper;
import com.familymoney.domains.transactions.controllers.mappers.CreateGroupResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.GroupDtoMapper;
import com.familymoney.domains.transactions.controllers.mappers.MemberDtoMapper;
import com.familymoney.domains.transactions.controllers.mappers.TransactionDtoMapper;
import com.familymoney.domains.transactions.controllers.mappers.UpdateGroupRequestMapper;
import com.familymoney.domains.transactions.services.GroupService;
import com.familymoney.domains.transactions.services.data.BalanceData;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.MemberData;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.utils.AuthenticationUtils;
import com.familymoney.utils.AuthorizedUser;
import com.familymoney.utils.PageResponse;
import com.familymoney.utils.PageResponseMapper;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultGroupAdminController implements GroupAdminController {

  private final GroupService groupService;

  @Override
  public CreateGroupResponseDto createEmptyGroup(final CreateGroupRequestDto request) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final GroupId groupId =
        groupService.createEmptyGroup(
            GroupName.fromString(request.name()),
            Description.of(request.description().trim()),
            Currency.getInstance(request.currencyCode()),
            user);
    return CreateGroupResponseMapper.toDto(groupId);
  }

  @Override
  public PageResponse<GroupDto> getGroupsForUser(
      final UUID userId, final int page, final int size) {
    final Page<GroupData> groupPages =
        groupService.getGroupsForUser(UserId.fromUuid(userId), page, size);
    return PageResponseMapper.from(groupPages.map(GroupDtoMapper::toDto));
  }

  @Override
  public void deleteGroup(final UUID groupId) {
    groupService.deleteGroupAsAdmin(GroupId.fromUuid(groupId));
  }

  @Override
  public GroupDto getGroupInfo(final UUID groupId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final GroupData groupData = groupService.getGroupInfo(GroupId.fromUuid(groupId), user);
    return GroupDtoMapper.toDto(groupData);
  }

  @Override
  public void updateGroupInfo(final UUID groupId, final UpdateGroupRequestDto request) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    groupService.updateGroupInfo(
        GroupId.fromUuid(groupId), user, UpdateGroupRequestMapper.fromDto(request));
  }

  @Override
  public void addUserToGroup(final UUID groupId, final UUID userIdToAdd) {
    groupService.addUserToGroupAsAdmin(GroupId.fromUuid(groupId), UserId.fromUuid(userIdToAdd));
  }

  @Override
  public void removeUserFromGroup(UUID groupId, UUID userId) {
    groupService.removeUserFromGroupAsAdmin(GroupId.fromUuid(groupId), UserId.fromUuid(userId));
  }

  @Override
  public List<MemberDto> getUsersInGroup(final UUID groupId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final List<MemberData> users = groupService.getUsersInGroup(GroupId.fromUuid(groupId), user);
    return users.stream().map(MemberDtoMapper::toDto).toList();
  }

  @Override
  public List<BalanceDto> getGroupBalances(final UUID groupId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final List<BalanceData> balances =
        groupService.getGroupBalances(GroupId.fromUuid(groupId), user);
    return balances.stream().map(BalanceDtoMapper::toDto).toList();
  }

  @Override
  public void forceSyncBalances(final UUID groupId) {
    groupService.forceSyncBalancesAsAdmin(GroupId.fromUuid(groupId));
  }

  @Override
  public PageResponse<TransactionDto> getGroupTransactions(
      final UUID groupId, final int page, final int size) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final Page<TransactionData> transactionPages =
        groupService.getGroupTransactions(GroupId.fromUuid(groupId), user, page, size);
    return PageResponseMapper.from(transactionPages.map(TransactionDtoMapper::toDto));
  }
}
