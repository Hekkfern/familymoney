package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.controllers.dtos.user.GetMyUserResponseDto;
import com.familymoney.familymoney.controllers.dtos.user.UpdateUserRequestDto;
import com.familymoney.familymoney.controllers.mappers.GetMyUserResponseMapper;
import com.familymoney.familymoney.controllers.mappers.UpdateUserRequestMapper;
import com.familymoney.familymoney.services.IUserService;
import com.familymoney.familymoney.utils.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
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
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
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
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Delete user
    userService.deleteUser(userId);
  }

  @Override
  public void updateMyUserInfo(UpdateUserRequestDto request) {
    // Get user ID from security context (validated)
    val userId = AuthenticationUtils.getUserIdFromSecurityContext();
    // Update user
    userService.updateUserInfo(userId, updateUserRequestMapper.fromDto(request));
  }
}
