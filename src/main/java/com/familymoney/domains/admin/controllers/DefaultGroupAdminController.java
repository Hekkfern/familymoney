package com.familymoney.domains.admin.controllers;

import com.familymoney.domains.transactions.controllers.dtos.BalanceDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GroupDto;
import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.mappers.CreateGroupResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.GroupDtoMapper;
import com.familymoney.domains.transactions.controllers.mappers.UpdateGroupRequestMapper;
import com.familymoney.domains.transactions.services.GroupService;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.utils.PageResponse;
import java.util.List;
import java.util.UUID;
import javax.money.Monetary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultGroupAdminController implements GroupAdminController {

  private final GroupService groupService;

  @Override
  public CreateGroupResponseDto createGroup(final CreateGroupRequestDto request) {
    final GroupId groupId =
        groupService.createGroup(
            GroupName.fromString(request.name()),
            Description.of(request.description().trim()),
            Monetary.getCurrency(request.currencyCode()));
    return CreateGroupResponseMapper.toDto(groupId);
  }

  @Override
  public List<UUID> getGroupsForUser(final UUID userId) {
    final Page<GroupData> groupPages =
        groupService.getGroupsByUser(UserId.fromUuid(userId), PageRequest.of(0, 10_000));
    return groupPages.getContent().stream().map(GroupDtoMapper::toDto).map(GroupDto::id).toList();
  }

  @Override
  public void deleteGroup(final UUID groupId) {
    groupService.deleteGroupAsAdmin(GroupId.fromUuid(groupId));
  }

  @Override
  public GroupDto getGroupInfo(final UUID groupId) {
    final GroupData groupData = groupService.getGroupInfoAsAdmin(GroupId.fromUuid(groupId));
    return GroupDtoMapper.toDto(groupData);
  }

  @Override
  public void updateGroupInfo(final UUID groupId, final UpdateGroupRequestDto request) {
    groupService.updateGroupInfoAsAdmin(
        GroupId.fromUuid(groupId), UpdateGroupRequestMapper.fromDto(request));
  }

  @Override
  public void addUserToGroup(final UUID groupId, final UUID userId) {
    groupService.addUserToGroupAsAdmin(GroupId.fromUuid(groupId), UserId.fromUuid(userId));
  }

  @Override
  public void removeUserFromGroup(final UUID groupId, final UUID userId) {
    groupService.removeUserFromGroupAsAdmin(GroupId.fromUuid(groupId), UserId.fromUuid(userId));
  }

  @Override
  public List<UUID> getUsersInGroup(final UUID groupId) {
    final List<UserId> users = groupService.getUsersInGroupAsAdmin(GroupId.fromUuid(groupId));
    return users.stream().map(UserId::value).toList();
  }

  @Override
  public List<BalanceDto> getGroupBalances(UUID groupId) {
    // TODO
    return List.of();
  }

  @Override
  public void forceSyncBalances(UUID groupId) {
    // TODO
  }

  @Override
  public PageResponse<TransactionDto> getGroupTransactions(UUID groupId, int page, int size) {
    // TODO
    return null;
  }
}
