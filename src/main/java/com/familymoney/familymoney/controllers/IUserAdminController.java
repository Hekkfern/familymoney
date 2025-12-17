package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.controllers.dtos.admin.GetUserResponseDto;
import com.familymoney.familymoney.controllers.dtos.admin.GetUserRoleResponseDto;
import com.familymoney.familymoney.controllers.dtos.admin.GetUsersResponseDto;
import com.familymoney.familymoney.controllers.dtos.user.UpdateUserRequestDto;
import com.familymoney.familymoney.types.UserId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/admin/users")
public interface IUserAdminController {

  @GetMapping("/{userId}")
  GetUserResponseDto getUser(@PathVariable @NotNull UserId userId);

  @GetMapping
  GetUsersResponseDto getUsers(Pageable pageable);

  @PutMapping("/{userId}/enable")
  void enableUser(@PathVariable @NotNull UserId userId, @RequestParam boolean enabled);

  @DeleteMapping("/{userId}")
  void deleteUser(@PathVariable @NotNull UserId userId);

  @PatchMapping("/{userId}")
  void updateUserInfo(
      @PathVariable @NotNull UserId userId, @RequestBody @Valid UpdateUserRequestDto request);

  @PutMapping("/{userId}/role")
  void setUserRole(@PathVariable @NotNull UserId userId, @RequestBody @NotBlank String role);

  @GetMapping("/{userId}/role")
  GetUserRoleResponseDto getUserRole(@PathVariable @NotNull UserId userId);
}
