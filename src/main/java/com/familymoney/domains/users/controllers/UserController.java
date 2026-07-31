package com.familymoney.domains.users.controllers;

import com.familymoney.domains.users.controllers.dtos.GetMyUserResponseDto;
import com.familymoney.domains.users.controllers.dtos.UpdateUserRequestDto;
import com.familymoney.domains.users.controllers.mappers.GetMyUserResponseMapper;
import com.familymoney.domains.users.controllers.mappers.UpdateUserRequestMapper;
import com.familymoney.domains.users.services.IUserService;
import com.familymoney.domains.users.services.data.UserData;
import com.familymoney.testutils.AuthenticationUtils;
import com.familymoney.testutils.AuthorizedUser;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements IUserController {

  private final IUserService userService;

  @Override
  public GetMyUserResponseDto getMyUserInfo() {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Fetch user data
    final UserData userData =
        userService
            .getUserData(user.id())
            .orElseThrow(
                () -> new NoSuchElementException("User not found for id: %s".formatted(user.id())));
    // Return response
    return GetMyUserResponseMapper.toDto(userData);
  }

  @Override
  public void deleteMyUser() {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Delete user
    userService.deleteUser(user.id());
  }

  @Override
  public void updateMyUserInfo(final UpdateUserRequestDto request) {
    // Get user ID from security context (validated)
    final AuthorizedUser user = AuthenticationUtils.getUserIdFromSecurityContext();
    // Update user
    userService.updateUserInfo(user.id(), UpdateUserRequestMapper.fromDto(request));
  }
}
