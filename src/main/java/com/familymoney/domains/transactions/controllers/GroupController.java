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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("groups")
public interface GroupController {

  @PostMapping(path = "", version = "1")
  CreateGroupResponseDto createGroup(@RequestBody @Valid CreateGroupRequestDto request);

  @GetMapping(path = "", version = "1")
  GetGroupsResponseDto getGroupsOfUser(Pageable pageable);

  @DeleteMapping(path = "{groupId}", version = "1")
  void deleteGroup(@PathVariable @NotNull UUID groupId);

  @GetMapping(path = "{groupId}", version = "1")
  GetGroupResponseDto getGroupInfo(@PathVariable @NotNull UUID groupId);

  @PatchMapping(path = "{groupId}", version = "1")
  void updateGroupInfo(
      @PathVariable @NotNull UUID groupId, @RequestBody @Valid UpdateGroupRequestDto request);

  @GetMapping(path = "{groupId}/invitation", version = "1")
  GetInvitationTokenResponseDto getInvitationToken(@PathVariable @NotNull UUID groupId);

  @PostMapping(path = "invitation", version = "1")
  void enterToGroup(@RequestBody @Valid EnterGroupRequestDto request);

  @GetMapping(path = "{groupId}/users", version = "1")
  GetUsersInGroupResponseDto getUsersInGroup(@PathVariable @NotNull UUID groupId);

  @DeleteMapping(path = "{groupId}/users", version = "1")
  void removeUserInGroup(
      @PathVariable @NotNull UUID groupId, @RequestBody @Valid RemoveUserInGroupRequestDto request);

  @GetMapping(path = "{groupId}/balances", version = "1")
  GetGroupBalancesResponseDto getGroupBalances(@PathVariable @NotNull UUID groupId);
}
