package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.controllers.dtos.group.*;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.TransactionId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/groups")
public interface ITransactionGroupController {

  @PostMapping(version = "1")
  CreateGroupResponseDto createGroup(@RequestBody @Valid CreateGroupRequestDto request);

  @DeleteMapping(path = "/{groupId}", version = "1")
  void deleteGroup(@PathVariable @NotNull GroupId groupId);

  @GetMapping(path = "/{groupId}", version = "1")
  GetGroupResponseDto getGroupInfo(@PathVariable @NotNull GroupId groupId);

  @PatchMapping(path = "/{groupId}", version = "1")
  void updateGroupInfo(
      @PathVariable @NotNull GroupId groupId, @RequestBody @Valid UpdateGroupRequestDto request);

  @GetMapping(path = "/{groupId}/users/share", version = "1")
  GetInvitationTokenResponseDto getInvitationToken(@PathVariable @NotNull GroupId groupId);

  @PostMapping(path = "/{groupId}/users/access", version = "1")
  void enterToGroup(
      @PathVariable @NotNull GroupId groupId, @RequestBody @Valid EnterGroupRequestDto request);

  @GetMapping(path = "/{groupId}/users", version = "1")
  GetUsersInGroupResponseDto getUsersInGroup(@PathVariable @NotNull GroupId groupId);

  @GetMapping(path = "/{groupId}/balances", version = "1")
  GetGroupBalancesResponseDto getGroupBalances(@PathVariable @NotNull GroupId groupId);

  @GetMapping(path = "/{groupId}/transactions", version = "1")
  GetTransactionsResponseDto getTransactions(
      @PathVariable @NotNull GroupId groupId, Pageable pageable);

  @PostMapping(path = "/{groupId}/transactions", version = "1")
  void createTransaction(@PathVariable @NotNull GroupId groupId);

  @PatchMapping(path = "/{groupId}/transactions/{transactionId}", version = "1")
  void updateTransaction(
      @PathVariable @NotNull GroupId groupId,
      @PathVariable @NotNull TransactionId transactionId,
      UpdateTransactionRequestDto request);

  @DeleteMapping(path = "/{groupId}/transactions/{transactionId}", version = "1")
  void deleteTransaction(
      @PathVariable @NotNull GroupId groupId, @PathVariable @NotNull TransactionId transactionId);
}
