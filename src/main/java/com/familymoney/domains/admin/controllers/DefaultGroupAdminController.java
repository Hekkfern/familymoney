package com.familymoney.domains.admin.controllers;

import com.familymoney.domains.transactions.controllers.dtos.CreateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupsResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetUsersInGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.mappers.CreateGroupResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.GetGroupResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.GetUsersInGroupResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.UpdateGroupRequestMapper;
import com.familymoney.domains.transactions.services.GroupService;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.UserId;
import java.util.List;
import java.util.UUID;
import javax.money.Monetary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultGroupAdminController implements GroupAdminController {

  private final GroupService groupService;

  @Override
  public CreateGroupResponseDto createGroup(CreateGroupRequestDto request) {
    final GroupId groupId =
        groupService.createGroup(
            GroupName.fromString(request.name()),
            Description.of(request.description().trim()),
            Monetary.getCurrency(request.currencyCode()));
    return CreateGroupResponseMapper.toDto(groupId);
  }

  @Override
  public GetGroupsResponseDto getGroupsOfUser(UUID userId, Pageable pageable) {
    final Page<GroupData> groupPages =
        groupService.getGroupsByUser(UserId.fromUuid(userId), pageable);
    return new GetGroupsResponseDto(
        groupPages.getContent().stream().map(GetGroupResponseMapper::toDto).toList());
  }

  @Override
  public void deleteGroup(UUID groupId) {
    groupService.deleteGroupAsAdmin(GroupId.fromUuid(groupId));
  }

  @Override
  public GetGroupResponseDto getGroupInfo(UUID groupId) {
    final GroupData groupData = groupService.getGroupInfoAsAdmin(GroupId.fromUuid(groupId));
    return GetGroupResponseMapper.toDto(groupData);
  }

  @Override
  public void updateGroupInfo(UUID groupId, UpdateGroupRequestDto request) {
    groupService.updateGroupInfoAsAdmin(
        GroupId.fromUuid(groupId), UpdateGroupRequestMapper.fromDto(request));
  }

  @Override
  public void addUserToGroup(UUID groupId, UUID userId) {
    groupService.addUserToGroupAsAdmin(GroupId.fromUuid(groupId), UserId.fromUuid(userId));
  }

  @Override
  public void removeUserFromGroup(UUID groupId, UUID userId) {
    groupService.removeUserFromGroupAsAdmin(GroupId.fromUuid(groupId), UserId.fromUuid(userId));
  }

  @Override
  public GetUsersInGroupResponseDto getUsersInGroup(UUID groupId) {
    final List<UserId> users = groupService.getUsersInGroupAsAdmin(GroupId.fromUuid(groupId));
    return GetUsersInGroupResponseMapper.toDto(users);
  }
}
