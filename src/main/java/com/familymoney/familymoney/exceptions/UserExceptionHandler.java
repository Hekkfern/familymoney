package com.familymoney.familymoney.exceptions;

import com.familymoney.familymoney.controllers.UserAdminController;
import com.familymoney.familymoney.controllers.UserController;
import com.familymoney.familymoney.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(
    assignableTypes = {UserAdminController.class, UserController.class, UserService.class})
public class UserExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  public ProblemDetail handleUserNotFoundException(UserNotFoundException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }
}
