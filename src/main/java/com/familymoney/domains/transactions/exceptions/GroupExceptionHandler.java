package com.familymoney.domains.transactions.exceptions;

import com.familymoney.domains.transactions.controllers.GroupController;
import com.familymoney.domains.transactions.services.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(assignableTypes = {GroupController.class, GroupService.class})
public class GroupExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(UserIsNotMemberOfGroupException.class)
  public ProblemDetail handleGroupNotOwnedByUserException(UserIsNotMemberOfGroupException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials");
  }

  @ExceptionHandler(GroupInvitationInvalidException.class)
  public ProblemDetail handleGroupInvitationNotFoundException(GroupInvitationInvalidException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(GroupOwnerNotFoundException.class)
  public ProblemDetail handleGroupOwnerNotFoundException(GroupOwnerNotFoundException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }
}
