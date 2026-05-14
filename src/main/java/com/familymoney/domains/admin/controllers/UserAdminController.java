package com.familymoney.domains.admin.controllers;

import com.familymoney.domains.user.controllers.dtos.GetUserResponseDto;
import com.familymoney.domains.user.controllers.dtos.GetUserRoleResponseDto;
import com.familymoney.domains.user.controllers.dtos.UpdateUserRequestDto;
import com.familymoney.domains.user.controllers.mappers.GetUserResponseMapper;
import com.familymoney.domains.user.controllers.mappers.UpdateUserRequestMapper;
import com.familymoney.domains.user.exceptions.UserNotFoundException;
import com.familymoney.domains.user.services.IUserService;
import com.familymoney.domains.user.types.Role;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.utils.PageResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  public PageResponse<GetUserResponseDto> getUsersInfo(
      final int page, final int size, final SortField sort, final Sort.Direction direction) {
    final Sort stableSort =
        Sort.by(direction, sort.toString().toLowerCase()).and(Sort.by(Sort.Direction.ASC, "id"));
    final Pageable pageable = PageRequest.of(page, size, stableSort);
    val userDataPages = userService.getUsers(pageable);
    return PageResponse.from(userDataPages.map(getUserResponseMapper::toDto));
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
