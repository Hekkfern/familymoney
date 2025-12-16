package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.controllers.dtos.user.GetMyUserResponseDto;
import com.familymoney.familymoney.controllers.dtos.user.UpdateUserRequestDto;
import com.familymoney.familymoney.controllers.mappers.GetMyUserResponseMapper;
import com.familymoney.familymoney.controllers.mappers.UpdateUserRequestMapper;
import com.familymoney.familymoney.services.IUserService;
import com.familymoney.familymoney.types.UserId;
import lombok.RequiredArgsConstructor;
import lombok.val;
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
  public GetMyUserResponseDto getMyUserInfo() {
    // Get user ID from security context (validated)
    UserId userId = getUserIdFromSecurityContext();
    // Fetch user data
    val userDataOpt = userService.getUserData(userId);
    // Return response or throw a clear exception if no user
    return getUserResponseMapper.toDto(
        userDataOpt.orElseThrow(
            () -> new java.util.NoSuchElementException("User not found for id: " + userId)));
  }

  @Override
  public void deleteMyUser() {
    // Get user ID from security context (validated)
    UserId userId = getUserIdFromSecurityContext();
    // Delete user
    userService.deleteUser(userId);
  }

  @Override
  public void updateMyUserInfo(UpdateUserRequestDto request) {
    // Get user ID from security context (validated)
    UserId userId = getUserIdFromSecurityContext();
    // Update user
    userService.updateUserInfo(userId, updateUserRequestMapper.fromDto(request));
  }

  // Helper to safely extract UserId from SecurityContext and throw a consistent exception if
  // missing
  private UserId getUserIdFromSecurityContext() {
    val authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new AuthenticationCredentialsNotFoundException("User ID not found in security context");
    }
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof UserId userId)) {
      throw new AuthenticationCredentialsNotFoundException("User ID not found in security context");
    }
    return userId;
  }
}
