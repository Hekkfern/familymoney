package com.familymoney.domains.admin.controllers;

import com.familymoney.domains.admin.controllers.dtos.AddUserToGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupsResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetUsersInGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.RemoveUserInGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public class DefaultGroupAdminController implements GroupAdminController {
  @Override
  public GetGroupsResponseDto getGroupsOfUser(UUID userId, Pageable pageable) {
    return null;
  }

  @Override
  public void deleteGroup(UUID groupId) {}

  @Override
  public GetGroupResponseDto getGroupInfo(UUID groupId) {
    return null;
  }

  @Override
  public void updateGroupInfo(UUID groupId, UpdateGroupRequestDto request) {}

  @Override
  public void addUserToGroup(UUID groupId, AddUserToGroupRequestDto request) {}

  @Override
  public void removeUserInGroup(UUID groupId, RemoveUserInGroupRequestDto request) {}

  @Override
  public GetUsersInGroupResponseDto getUsersInGroup(UUID groupId) {
    return null;
  }
}
