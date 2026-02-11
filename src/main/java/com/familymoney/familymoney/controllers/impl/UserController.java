package com.familymoney.familymoney.controllers.impl;

import com.familymoney.familymoney.controllers.IUserController;
import com.familymoney.familymoney.controllers.dtos.user.GetMyUserResponseDto;
import com.familymoney.familymoney.controllers.dtos.user.UpdateUserRequestDto;
import com.familymoney.familymoney.controllers.mappers.user.GetMyUserResponseMapper;
import com.familymoney.familymoney.controllers.mappers.user.UpdateUserRequestMapper;
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
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Fetch user data
    val userData =
        userService
            .getUserData(user.id())
            .orElseThrow(
                () -> new java.util.NoSuchElementException("User not found for id: " + user.id()));
    // Return response
    return getUserResponseMapper.toDto(userData);
  }

  @Override
  public void deleteMyUser() {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Delete user
    userService.deleteUser(user.id());
  }

  @Override
  public void updateMyUserInfo(UpdateUserRequestDto request) {
    // Get user ID from security context (validated)
    val user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Update user
    userService.updateUserInfo(user.id(), updateUserRequestMapper.fromDto(request));
  }
}
