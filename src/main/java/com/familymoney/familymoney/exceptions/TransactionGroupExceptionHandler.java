package com.familymoney.familymoney.exceptions;

import com.familymoney.familymoney.controllers.GroupController;
import com.familymoney.familymoney.services.TransactionGroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(
    assignableTypes = {GroupController.class, TransactionGroupService.class})
public class TransactionGroupExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(GroupNotOwnedByUserException.class)
  public ProblemDetail handleGroupNotOwnedByUserException(GroupNotOwnedByUserException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials");
  }

    @ExceptionHandler(GroupInvitationNotFoundException.class)
    public ProblemDetail handleGroupInvitationNotFoundException(GroupInvitationNotFoundException e) {
        logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
