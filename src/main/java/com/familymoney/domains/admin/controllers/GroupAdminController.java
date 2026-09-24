package com.familymoney.domains.admin.controllers;

import com.familymoney.domains.transactions.controllers.dtos.BalanceDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GroupDto;
import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import com.familymoney.utils.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("admin/groups")
public interface GroupAdminController {

  @Operation(summary = "Create a new transaction group")
  @PostMapping(path = "", version = "1")
  CreateGroupResponseDto createGroup(@RequestBody @Valid CreateGroupRequestDto request);

  @Operation(summary = "Get the list of groups where the selected user is a member")
  @GetMapping(path = "users/{userId}", version = "1")
  List<UUID> getGroupsForUser(@PathVariable @NotNull UUID userId);

  @Operation(summary = "Delete a group")
  @DeleteMapping(path = "{groupId}", version = "1")
  void deleteGroup(@PathVariable @NotNull UUID groupId);

  @Operation(summary = "Get information about a specific group")
  @GetMapping(path = "{groupId}", version = "1")
  GroupDto getGroupInfo(@PathVariable @NotNull UUID groupId);

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
  List<UUID> getUsersInGroup(@PathVariable @NotNull UUID groupId);

  @GetMapping(path = "{groupId}/balances", version = "1")
  List<BalanceDto> getGroupBalances(@PathVariable @NotNull UUID groupId);

  @Operation(summary = "Recalculates the balances of all the expenses in the group")
  @PostMapping(path = "{groupId}", version = "1")
  void forceSyncBalances(@PathVariable @NotNull UUID groupId);

  @GetMapping(path = "{groupId}/transactions", version = "1")
  PageResponse<TransactionDto> getGroupTransactions(
      @PathVariable @NotNull UUID groupId,
      @RequestParam(defaultValue = "0") @Min(0) @Max(10_000) int page,
      @RequestParam(defaultValue = "20") @Min(20) @Max(100) int size);
}
