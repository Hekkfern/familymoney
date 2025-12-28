package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.controllers.dtos.admin.GetUserResponseDto;
import com.familymoney.familymoney.controllers.dtos.admin.GetUserRoleResponseDto;
import com.familymoney.familymoney.controllers.dtos.admin.GetUsersResponseDto;
import com.familymoney.familymoney.controllers.dtos.user.UpdateUserRequestDto;
import com.familymoney.familymoney.controllers.mappers.GetUserResponseMapper;
import com.familymoney.familymoney.controllers.mappers.UpdateUserRequestMapper;
import com.familymoney.familymoney.exceptions.UserNotFoundException;
import com.familymoney.familymoney.services.IUserService;
import com.familymoney.familymoney.types.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserAdminController implements IUserAdminController {

  private final IUserService userService;
  private final GetUserResponseMapper getUserResponseMapper;
  private final UpdateUserRequestMapper updateUserRequestMapper;

  @Override
  public GetUserResponseDto getUserInfo(UUID userId) {
    // Fetch user data
    val userData =
        userService
            .getUserData(UserId.fromUuid(userId))
            .orElseThrow(() -> new UserNotFoundException("User not found"));
    // Return response
    return getUserResponseMapper.toDto(userData);
  }

  @Override
  public GetUsersResponseDto getUsersInfo(Pageable pageable) {
    val userDataPages = userService.getUsers(pageable);
    return new GetUsersResponseDto(
        userDataPages.getContent().stream().map(getUserResponseMapper::toDto).toList());
  }

  @Override
  public void enableUser(UUID userId, boolean enabled) {
    userService.enableUser(UserId.fromUuid(userId), enabled);
  }

  @Override
  public void deleteUser(UUID userId) {
    userService.deleteUser(UserId.fromUuid(userId));
  }

  @Override
  public void updateUserInfo(UUID userId, UpdateUserRequestDto request) {
    userService.updateUserInfo(UserId.fromUuid(userId), updateUserRequestMapper.fromDto(request));
  }

  @Override
  public void setUserRole(UUID userId, String role) {
    userService.setUserRole(UserId.fromUuid(userId), Role.fromString(role));
  }

  @Override
  public GetUserRoleResponseDto getUserRole(UUID userId) {
    return userService
        .getUserRole(UserId.fromUuid(userId))
        .map(GetUserRoleResponseDto::new)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
  }
}
