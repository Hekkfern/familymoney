package com.familymoney.domains.transactions.controllers;

import com.familymoney.domains.transactions.controllers.dtos.CreateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.EnterGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupBalancesResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupsResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetInvitationTokenResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetUsersInGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.RemoveUserRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequestMapping
public interface IGroupController {

  @PostMapping(path = "groups", version = "1")
  CreateGroupResponseDto createGroup(@RequestBody @Valid CreateGroupRequestDto request);

  @GetMapping(path = "groups", version = "1")
  GetGroupsResponseDto getGroupsOfUser(Pageable pageable);

  @DeleteMapping(path = "groups/{groupId}", version = "1")
  void deleteGroup(@PathVariable @NotNull UUID groupId);

  @GetMapping(path = "groups/{groupId}", version = "1")
  GetGroupResponseDto getGroupInfo(@PathVariable @NotNull UUID groupId);

  @PatchMapping(path = "groups/{groupId}", version = "1")
  void updateGroupInfo(
      @PathVariable @NotNull UUID groupId, @RequestBody @Valid UpdateGroupRequestDto request);

  @GetMapping(path = "groups/{groupId}/invitation", version = "1")
  GetInvitationTokenResponseDto getInvitationToken(@PathVariable @NotNull UUID groupId);

  @PostMapping(path = "groups/invitation", version = "1")
  void enterToGroup(@RequestBody @Valid EnterGroupRequestDto request);

  @GetMapping(path = "groups/{groupId}/users", version = "1")
  GetUsersInGroupResponseDto getUsersInGroup(@PathVariable @NotNull UUID groupId);

  @DeleteMapping(path = "groups/{groupId}/users", version = "1")
  void removeUserInGroup(
      @PathVariable @NotNull UUID groupId, @RequestBody @Valid RemoveUserRequestDto request);

  @GetMapping(path = "groups/{groupId}/balances", version = "1")
  GetGroupBalancesResponseDto getGroupBalances(@PathVariable @NotNull UUID groupId);
}
