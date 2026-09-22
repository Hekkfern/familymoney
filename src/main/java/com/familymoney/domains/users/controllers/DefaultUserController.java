package com.familymoney.domains.users.controllers;

import com.familymoney.domains.users.controllers.dtos.UpdateUserRequestDto;
import com.familymoney.domains.users.controllers.dtos.UserDto;
import com.familymoney.domains.users.controllers.mappers.UpdateUserRequestMapper;
import com.familymoney.domains.users.controllers.mappers.UserDtoMapper;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.services.UserService;
import com.familymoney.domains.users.services.data.UserData;
import com.familymoney.utils.AuthenticationUtils;
import com.familymoney.utils.AuthorizedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultUserController implements UserController {

  private final UserService userService;

  @Override
  public UserDto getMyUserInfo() {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final UserData userData =
        userService
            .getUserData(user.id())
            .orElseThrow(
                () -> new UserNotFoundException("User not found for id: %s".formatted(user.id())));
    return UserDtoMapper.toDto(userData);
  }

  @Override
  public void deleteMyUser() {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    userService.deleteUser(user.id());
  }

  @Override
  public void updateMyUserInfo(final UpdateUserRequestDto request) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    userService.updateUserInfo(user.id(), UpdateUserRequestMapper.fromDto(request));
  }
}
