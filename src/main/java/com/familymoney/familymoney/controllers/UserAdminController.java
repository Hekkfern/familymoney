package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.controllers.dtos.admin.GetUserResponseDto;
import com.familymoney.familymoney.controllers.dtos.admin.GetUsersResponseDto;
import com.familymoney.familymoney.controllers.dtos.user.UpdateUserRequestDto;
import com.familymoney.familymoney.controllers.mappers.GetUserResponseMapper;
import com.familymoney.familymoney.controllers.mappers.UpdateUserRequestMapper;
import com.familymoney.familymoney.exceptions.UserNotFoundException;
import com.familymoney.familymoney.services.IUserService;
import com.familymoney.familymoney.types.*;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserAdminController implements IUserAdminController {

  private final IUserService userService;
  private final GetUserResponseMapper getUserResponseMapper;
  private final UpdateUserRequestMapper updateUserRequestMapper;

  @Override
  public @NonNull GetUserResponseDto getUser(UserId userId) {
    // Fetch user data
    val userDataOpt = userService.getUserData(userId);
    if (userDataOpt.isEmpty()) {
      throw new UserNotFoundException("User with ID " + userId.value() + " not found");
    }
    val userData = userDataOpt.get();
    // Return response
    return getUserResponseMapper.toDto(userData);
  }

  @Override
  public @NonNull GetUsersResponseDto getUsers(Pageable pageable) {
    val userDataPages = userService.getUsers(pageable);
    return new GetUsersResponseDto(
        userDataPages.getContent().stream().map(getUserResponseMapper::toDto).toList());
  }

  @Override
  public void enableUser(UserId userId, boolean enabled) {
    userService.enableUser(userId, enabled);
  }

  @Override
  public void deleteUser(UserId userId) {
    userService.deleteUser(userId);
  }

  @Override
  public void updateUserInfo(UserId userId, UpdateUserRequestDto request) {
    userService.updateUserInfo(userId, updateUserRequestMapper.fromDto(request));
  }

  @Override
  public void setUserRole(UserId userId, String role) {
    userService.setUserRole(userId, Role.fromString(role));
  }

  @Override
  public void getUserRole(UserId userId) {
    userService.getUserRole(userId);
  }
}
