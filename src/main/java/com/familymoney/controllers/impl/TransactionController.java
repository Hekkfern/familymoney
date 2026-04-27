package com.familymoney.controllers.impl;

import com.familymoney.controllers.ITransactionController;
import com.familymoney.controllers.dtos.grouptransaction.*;
import com.familymoney.controllers.mappers.grouptransaction.*;
import com.familymoney.services.ITransactionGroupService;
import com.familymoney.types.*;
import com.familymoney.utils.AuthenticationUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionController implements ITransactionController {

  private final ITransactionGroupService transactionGroupService;
  private final GetGroupTransactionsResponseMapper getGroupTransactionsResponseMapper;
  private final UpdateTransactionRequestMapper updateTransactionRequestMapper;

  @Override
  public GetTransactionsResponseDto getGroupTransactions(UUID groupId, Pageable pageable) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Get balances
    val transactionPages =
        transactionGroupService.getGroupTransactions(
            GroupId.fromUuid(groupId), user.id(), pageable);
    // Generate response
    return getGroupTransactionsResponseMapper.toDto(transactionPages);
  }

  @Override
  public void createTransaction(UUID groupId, CreateTransactionRequestDto request) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Create transaction
    transactionGroupService.createTransactionInGroup(
        GroupId.fromUuid(groupId),
        request.description(),
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
        updateTransactionRequestMapper.fromDto(request));
  }

  @Override
  public void deleteTransaction(UUID transactionId) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Delete transaction
    transactionGroupService.deleteTransaction(user.id(), TransactionId.fromUuid(transactionId));
  }
}
