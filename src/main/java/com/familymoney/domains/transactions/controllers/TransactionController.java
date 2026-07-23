package com.familymoney.domains.transactions.controllers;

import com.familymoney.domains.transactions.controllers.dtos.CreateTransactionRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.GetTransactionsResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateTransactionRequestDto;
import com.familymoney.domains.transactions.controllers.mappers.GetGroupTransactionsResponseMapper;
import com.familymoney.domains.transactions.controllers.mappers.UpdateTransactionRequestMapper;
import com.familymoney.domains.transactions.services.ITransactionGroupService;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.TransactionId;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.testutils.AuthenticationUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionController implements ITransactionController {

  private final ITransactionGroupService transactionGroupService;

  @Override
  public GetTransactionsResponseDto getGroupTransactions(UUID groupId, Pageable pageable) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Get balances
    val transactionPages =
        transactionGroupService.getGroupTransactions(
            GroupId.fromUuid(groupId), user.id(), pageable);
    // Generate response
    return GetGroupTransactionsResponseMapper.toDto(transactionPages);
  }

  @Override
  public void createTransaction(UUID groupId, CreateTransactionRequestDto request) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Create transaction
    transactionGroupService.createTransactionInGroup(
        GroupId.fromUuid(groupId),
        Description.of(request.description()),
        UserId.fromUuid(request.from()),
        UserId.fromUuid(request.to()),
        request.amount(),
        request.doneAt(),
        user.id());
  }

  @Override
  public void updateTransaction(UUID transactionId, UpdateTransactionRequestDto request) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Update transaction
    transactionGroupService.updateTransaction(
        user.id(),
        TransactionId.fromUuid(transactionId),
        UpdateTransactionRequestMapper.fromDto(request));
  }

  @Override
  public void deleteTransaction(UUID transactionId) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Delete transaction
    transactionGroupService.deleteTransaction(user.id(), TransactionId.fromUuid(transactionId));
  }
}
