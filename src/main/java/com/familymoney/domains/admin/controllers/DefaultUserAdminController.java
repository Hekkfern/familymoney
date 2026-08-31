package com.familymoney.domains.admin.controllers;

import com.familymoney.domains.users.controllers.dtos.GetUserResponseDto;
import com.familymoney.domains.users.controllers.dtos.GetUserRoleResponseDto;
import com.familymoney.domains.users.controllers.dtos.UpdateUserRequestDto;
import com.familymoney.domains.users.controllers.mappers.GetUserResponseMapper;
import com.familymoney.domains.users.controllers.mappers.UpdateUserRequestMapper;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.services.UserService;
import com.familymoney.domains.users.services.data.UserData;
import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.utils.PageResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultUserAdminController implements UserAdminController {

  private final UserService userService;

  @Override
  public GetUserResponseDto getUserInfo(UUID userId) {
    // Fetch user data
    final UserData userData =
        userService
            .getUserData(UserId.fromUuid(userId))
            .orElseThrow(() -> new UserNotFoundException("User not found"));
    // Return response
    return GetUserResponseMapper.toDto(userData);
  }

  @Override
  public PageResponse<GetUserResponseDto> getUsersInfo(
      final int page, final int size, final SortField sort, final Sort.Direction direction) {
    final Sort stableSort =
        Sort.by(direction, sort.toString().toLowerCase()).and(Sort.by(Sort.Direction.ASC, "id"));
    final Pageable pageable = PageRequest.of(page, size, stableSort);
    final Page<UserData> userDataPages = userService.getUsers(pageable);
    return PageResponse.from(userDataPages.map(GetUserResponseMapper::toDto));
  }

  @Override
  public void enableUser(final UUID userId, boolean enabled) {
    userService.enableUser(UserId.fromUuid(userId), enabled);
  }

  @Override
  public void deleteUser(final UUID userId) {
    userService.deleteUser(UserId.fromUuid(userId));
  }

  @Override
  public void updateUserInfo(final UUID userId, final UpdateUserRequestDto request) {
    userService.updateUserInfo(UserId.fromUuid(userId), UpdateUserRequestMapper.fromDto(request));
  }

  @Override
  public void setUserRole(final UUID userId, final String role) {
    userService.setUserRole(UserId.fromUuid(userId), Role.fromString(role));
  }

  @Override
  public GetUserRoleResponseDto getUserRole(final UUID userId) {
    return userService
        .getUserRole(UserId.fromUuid(userId))
        .map(GetUserRoleResponseDto::new)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
  }
}
