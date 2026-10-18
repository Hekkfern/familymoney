package com.familymoney.domains.transactions.exceptions;

import com.familymoney.domains.transactions.controllers.ExpenseController;
import com.familymoney.domains.transactions.controllers.PaymentController;
import com.familymoney.domains.transactions.services.ExpenseService;
import com.familymoney.domains.transactions.services.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(assignableTypes = {ExpenseController.class, ExpenseService.class, PaymentController.class, PaymentService.class})
public class TransactionExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(PaymentNotFoundException.class)
  public ProblemDetail handlePaymentNotFoundException(PaymentNotFoundException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(ExpenseNotFoundException.class)
  public ProblemDetail handleExpenseNotFoundException(ExpenseNotFoundException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }
}
