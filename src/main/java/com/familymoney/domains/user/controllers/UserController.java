package com.familymoney.domains.user.controllers;

import com.familymoney.domains.user.controllers.dtos.GetMyUserResponseDto;
import com.familymoney.domains.user.controllers.dtos.UpdateUserRequestDto;
import com.familymoney.domains.user.controllers.mappers.GetMyUserResponseMapper;
import com.familymoney.domains.user.controllers.mappers.UpdateUserRequestMapper;
import com.familymoney.domains.user.services.IUserService;
import com.familymoney.utils.AuthenticationUtils;
import java.util.NoSuchElementException;
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
                () -> new NoSuchElementException("User not found for id: %s".formatted(user.id())));
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
