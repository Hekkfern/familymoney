package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.controllers.dtos.group.*;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.TransactionId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/groups")
public interface ITransactionGroupController {

  @PostMapping
  CreateGroupResponseDto createGroup(@RequestBody @Valid CreateGroupRequestDto request);

  @DeleteMapping("/{groupId}")
  void deleteGroup(@PathVariable @NotNull GroupId groupId);

  @GetMapping("/{groupId}")
  GetGroupResponseDto getGroupInfo(@PathVariable @NotNull GroupId groupId);

  @PatchMapping("/{groupId}")
  void updateGroupInfo(
      @PathVariable @NotNull GroupId groupId, @RequestBody @Valid UpdateGroupRequestDto request);

  @GetMapping("/{groupId}/users/share")
  GetInvitationTokenResponseDto getInvitationToken(@PathVariable @NotNull GroupId groupId);

  @PostMapping("/{groupId}/users/access")
  void enterToGroup(
      @PathVariable @NotNull GroupId groupId, @RequestBody @Valid EnterGroupRequestDto request);

  @GetMapping("/{groupId}/users")
  GetUsersInGroupResponseDto getUsersInGroup(@PathVariable @NotNull GroupId groupId);

  @GetMapping("/{groupId}/balances")
  GetGroupBalancesResponseDto getGroupBalances(@PathVariable @NotNull GroupId groupId);

  @GetMapping("/{groupId}/transactions")
  GetTransactionsResponseDto getTransactions(
      @PathVariable @NotNull GroupId groupId, Pageable pageable);

  @PostMapping("/{groupId}/transactions")
  void createTransaction(@PathVariable @NotNull GroupId groupId);

  @PatchMapping("/{groupId}/transactions/{transactionId}")
  void updateTransaction(
      @PathVariable @NotNull GroupId groupId,
      @PathVariable @NotNull TransactionId transactionId,
      UpdateTransactionRequestDto request);

  @DeleteMapping("/{groupId}/transactions/{transactionId}")
  void deleteTransaction(
      @PathVariable @NotNull GroupId groupId, @PathVariable @NotNull TransactionId transactionId);
}
