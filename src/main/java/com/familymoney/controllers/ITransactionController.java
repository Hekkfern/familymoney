package com.familymoney.controllers;

import com.familymoney.controllers.dtos.grouptransaction.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequestMapping
public interface ITransactionController {

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
