package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.controllers.dtos.group.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequestMapping("groups")
public interface ITransactionGroupController {

  @PostMapping(path = "", version = "1")
  CreateGroupResponseDto createGroup(@RequestBody @Valid CreateGroupRequestDto request);

  @DeleteMapping(path = "{groupId}", version = "1")
  void deleteGroup(@PathVariable @NotNull UUID groupId);

  @GetMapping(path = "{groupId}", version = "1")
  GetGroupResponseDto getGroupInfo(@PathVariable @NotNull UUID groupId);

  @PatchMapping(path = "{groupId}", version = "1")
  void updateGroupInfo(
      @PathVariable @NotNull UUID groupId, @RequestBody @Valid UpdateGroupRequestDto request);

  @GetMapping(path = "{groupId}/users/share", version = "1")
  GetInvitationTokenResponseDto getInvitationToken(@PathVariable @NotNull UUID groupId);

  @PostMapping(path = "{groupId}/users/access", version = "1")
  void enterToGroup(
      @PathVariable @NotNull UUID groupId, @RequestBody @Valid EnterGroupRequestDto request);

  @GetMapping(path = "{groupId}/users", version = "1")
  GetUsersInGroupResponseDto getUsersInGroup(@PathVariable @NotNull UUID groupId);

  @DeleteMapping(path = "{groupId}/users", version = "1")
  GetUsersInGroupResponseDto getUsersInGroup(
      @PathVariable @NotNull UUID groupId, @RequestBody @Valid RemoveUserRequestDto request);

  @GetMapping(path = "{groupId}/balances", version = "1")
  GetGroupBalancesResponseDto getGroupBalances(@PathVariable @NotNull UUID groupId);

  @GetMapping(path = "{groupId}/transactions", version = "1")
  GetTransactionsResponseDto getTransactions(
      @PathVariable @NotNull UUID groupId, Pageable pageable);

  @PostMapping(path = "{groupId}/transactions", version = "1")
  void createTransaction(@PathVariable @NotNull UUID groupId);

  @PatchMapping(path = "{groupId}/transactions/{transactionId}", version = "1")
  void updateTransaction(
      @PathVariable @NotNull UUID groupId,
      @PathVariable @NotNull UUID transactionId,
      UpdateTransactionRequestDto request);

  @DeleteMapping(path = "{groupId}/transactions/{transactionId}", version = "1")
  void deleteTransaction(
      @PathVariable @NotNull UUID groupId, @PathVariable @NotNull UUID transactionId);
}
