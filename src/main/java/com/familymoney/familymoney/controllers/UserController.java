package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.controllers.mappers.GetMyUserResponseMapper;
import com.familymoney.familymoney.controllers.mappers.UpdateUserRequestMapper;
import com.familymoney.familymoney.controllers.dtos.user.GetMyUserResponseDto;
import com.familymoney.familymoney.controllers.dtos.user.UpdateUserRequestDto;
import com.familymoney.familymoney.services.IUserService;
import com.familymoney.familymoney.types.UserId;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements IUserController {

  private final IUserService userService;
  private final GetMyUserResponseMapper getUserResponseMapper;
  private final UpdateUserRequestMapper updateUserRequestMapper;

  @Override
  @NonNull
  public GetMyUserResponseDto getMyUserInfo() {
    // Get user ID from security context
    val userIdFromCxt = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(userIdFromCxt instanceof UserId userId)) {
      throw new AuthenticationCredentialsNotFoundException("User ID not found in security context");
    }
    // Fetch user data
    val userDataOpt = userService.getUserData(userId);
    // Return response
    return getUserResponseMapper.toDto(userDataOpt.get());
  }

  @Override
  public void deleteMyUser() {
    // Get user ID from security context
    val userIdFromCxt = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(userIdFromCxt instanceof UserId userId)) {
      throw new AuthenticationCredentialsNotFoundException("User ID not found in security context");
    }
    // Delete user
    userService.deleteUser(userId);
  }

  @Override
  public void updateMyUserInfo(UpdateUserRequestDto request) {
    // Get user ID from security context
    val userIdFromCxt = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(userIdFromCxt instanceof UserId userId)) {
      throw new AuthenticationCredentialsNotFoundException("User ID not found in security context");
    }
    // Update user
    userService.updateUserInfo(userId, updateUserRequestMapper.fromDto(request));
  }
}
