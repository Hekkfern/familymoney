package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.controllers.dtos.grouptransaction.*;
import com.familymoney.familymoney.controllers.mappers.grouptransaction.*;
import com.familymoney.familymoney.services.ITransactionGroupService;
import com.familymoney.familymoney.types.*;
import com.familymoney.familymoney.utils.AuthenticationUtils;
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
    val transactions =
        transactionGroupService.getGroupTransactions(GroupId.fromUuid(groupId), user.id(), pageable);
    // Generate response
    return getGroupTransactionsResponseMapper.toDto(transactions);
  }

  @Override
  public void createTransaction(UUID groupId, CreateTransactionRequestDto request) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Create transaction
    transactionGroupService.createTransactionInGroup(
        GroupId.fromUuid(groupId),
            request.description(), request.from(), request.to(), request.amount(), request.doneAt(), user.id()
    );
  }

  @Override
  public void updateTransaction(UUID transactionId, UpdateTransactionRequestDto request) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Update transaction
    transactionGroupService.updateTransaction(
        user.id(), transactionId, updateTransactionRequestMapper.fromDto(request));
  }

  @Override
  public void deleteTransaction(UUID transactionId) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Delete transaction
    transactionGroupService.deleteTransaction(user.id(), TransactionId.fromUuid(transactionId));
  }
}
