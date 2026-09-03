package com.familymoney.domains.admin.controllers;

import com.familymoney.domains.transactions.controllers.dtos.CreateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupsResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetUsersInGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import io.swagger.v3.oas.annotations.Operation;
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

@RequestMapping("admin/groups")
public interface GroupAdminController {

  @Operation(summary = "Create a new transaction group")
  @PostMapping(path = "", version = "1")
  CreateGroupResponseDto createGroup(@RequestBody @Valid CreateGroupRequestDto request);

  @Operation(summary = "Get the list of groups where the selected user is a member")
  @GetMapping(path = "users/{userId}", version = "1")
  GetGroupsResponseDto getGroupsOfUser(@PathVariable @NotNull UUID userId, Pageable pageable);

  @Operation(summary = "Delete a group")
  @DeleteMapping(path = "{groupId}", version = "1")
  void deleteGroup(@PathVariable @NotNull UUID groupId);

  @Operation(summary = "Get information about a specific group")
  @GetMapping(path = "{groupId}", version = "1")
  GetGroupResponseDto getGroupInfo(@PathVariable @NotNull UUID groupId);

  @Operation(summary = "Update information of a specific group")
  @PatchMapping(path = "{groupId}", version = "1")
  void updateGroupInfo(
      @PathVariable @NotNull UUID groupId, @RequestBody @Valid UpdateGroupRequestDto request);

  @Operation(summary = "Add a user to a specific group")
  @PostMapping(path = "{groupId}/users/{userId}", version = "1")
  void addUserToGroup(@PathVariable @NotNull UUID groupId, @PathVariable @NotNull UUID userId);

  @Operation(summary = "Remove a user from a specific group")
  @DeleteMapping(path = "{groupId}/users/{userId}", version = "1")
  void removeUserFromGroup(@PathVariable @NotNull UUID groupId, @PathVariable @NotNull UUID userId);

  @Operation(summary = "Get the list of users in a specific group")
  @GetMapping(path = "{groupId}/users", version = "1")
  GetUsersInGroupResponseDto getUsersInGroup(@PathVariable @NotNull UUID groupId);
}
