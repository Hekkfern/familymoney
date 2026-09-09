package com.familymoney.domains.transactions.exceptions;

import com.familymoney.domains.transactions.controllers.TransactionController;
import com.familymoney.domains.transactions.services.TransactionService;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(assignableTypes = {TransactionController.class, TransactionService.class})
public class TransactionExceptionHandler extends ResponseEntityExceptionHandler {

  
}
