package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.dtos.user.GetMyUserResponseDto;
import com.familymoney.familymoney.dtos.user.UpdateUserRequestDto;
import com.familymoney.familymoney.services.IUserService;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Password;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.Username;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements IUserController {

  private final IUserService userService;

  @Override
  @NonNull
  public GetMyUserResponseDto getMyUser() {
    // Get user ID from security context
    val userIdFromCxt = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(userIdFromCxt instanceof UserId userId)) {
      throw new AuthenticationCredentialsNotFoundException("User ID not found in security context");
    }
    // Fetch user data
    val userData = userService.getMyUserData(userId);
    // Return response
    return new GetMyUserResponseDto(
        userData.username().toString(), userData.email().toString(), userData.createdAt());
  }

  @Override
  public void deleteMyUser() {
    // Get user ID from security context
    val userIdFromCxt = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(userIdFromCxt instanceof UserId userId)) {
      throw new AuthenticationCredentialsNotFoundException("User ID not found in security context");
    }
    // Delete user
    userService.deleteMyUser(userId);
  }

  @Override
  public void updateMyUser(UpdateUserRequestDto request) {
    // Get user ID from security context
    val userIdFromCxt = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(userIdFromCxt instanceof UserId userId)) {
      throw new AuthenticationCredentialsNotFoundException("User ID not found in security context");
    }
    // Delete user
    userService.updateMyUser(
        userId,
        request.username() != null
            ? Optional.of(request.username()).map(Username::new)
            : Optional.empty(),
        request.email() != null ? Optional.of(request.email()).map(Email::new) : Optional.empty(),
        request.password() != null
            ? Optional.of(request.password()).map(Password::new)
            : Optional.empty());
  }
}
