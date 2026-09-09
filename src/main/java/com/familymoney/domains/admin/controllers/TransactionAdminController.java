package com.familymoney.domains.admin.controllers;

import com.familymoney.domains.transactions.controllers.dtos.CreateTransactionRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupBalancesResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetTransactionsResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateTransactionRequestDto;
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

@RequestMapping("admin")
public interface TransactionAdminController {

  @GetMapping(path = "groups/{groupId}/balances", version = "1")
  GetGroupBalancesResponseDto getGroupBalances(@PathVariable @NotNull UUID groupId);

  @Operation(summary = "Recalculates the balances of all the transactions in the group")
  @PostMapping(path = "groups/{groupId}", version = "1")
  void forceSyncBalances(@PathVariable @NotNull UUID groupId);

  @GetMapping(path = "groups/{groupId}/transactions", version = "1")
  GetTransactionsResponseDto getGroupTransactions(
      @PathVariable @NotNull UUID groupId, Pageable pageable);

  @PostMapping(path = "groups/{groupId}/transactions", version = "1")
  void createTransaction(
      @PathVariable @NotNull UUID groupId, @RequestBody @Valid CreateTransactionRequestDto request);

  @PatchMapping(path = "transactions/{transactionId}", version = "1")
  void updateTransaction(
      @PathVariable @NotNull UUID transactionId,
      @RequestBody @Valid UpdateTransactionRequestDto request);

  @DeleteMapping(path = "transactions/{transactionId}", version = "1")
  void deleteTransaction(@PathVariable @NotNull UUID transactionId);
}
