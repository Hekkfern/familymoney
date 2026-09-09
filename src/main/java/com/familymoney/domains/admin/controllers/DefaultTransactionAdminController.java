package com.familymoney.domains.admin.controllers;

import com.familymoney.domains.transactions.controllers.dtos.CreateTransactionRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupBalancesResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetTransactionsResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateTransactionRequestDto;
import com.familymoney.domains.transactions.controllers.mappers.GetGroupBalancesResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.GetGroupTransactionsResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.UpdateTransactionRequestMapper;
import com.familymoney.domains.transactions.services.TransactionService;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.TransactionId;
import com.familymoney.domains.users.types.UserId;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class DefaultTransactionAdminController implements TransactionAdminController {

  private final TransactionService transactionService;

  @Override
  public GetGroupBalancesResponseDto getGroupBalances(final UUID groupId) {
    final Map<UserId, Money> balances =
        transactionService.getAllGroupBalancesAsAdmin(GroupId.fromUuid(groupId));
    return GetGroupBalancesResponseMapper.toDto(balances);
  }

  @Override
  public void forceSyncBalances(final UUID groupId) {
    // TODO
  }

  @Override
  public GetTransactionsResponseDto getGroupTransactions(
      final UUID groupId, final Pageable pageable) {
    final Page<TransactionData> transactionPages =
        transactionService.getGroupTransactionsAsAdmin(GroupId.fromUuid(groupId), pageable);
    return GetGroupTransactionsResponseMapper.toDto(transactionPages);
  }

  @Override
  public void createTransaction(final UUID groupId, final CreateTransactionRequestDto request) {
    transactionService.createTransactionInGroupAsAdmin(
        GroupId.fromUuid(groupId),
        Description.of(request.description()),
        UserId.fromUuid(request.from()),
        UserId.fromUuid(request.to()),
        request.amount(),
        request.doneAt());
  }

  @Override
  public void updateTransaction(
      final UUID transactionId, final UpdateTransactionRequestDto request) {
    transactionService.updateTransactionAsAdmin(
        TransactionId.fromUuid(transactionId), UpdateTransactionRequestMapper.fromDto(request));
  }

  @Override
  public void deleteTransaction(final UUID transactionId) {
    transactionService.deleteTransactionAsAdmin(TransactionId.fromUuid(transactionId));
  }
}
