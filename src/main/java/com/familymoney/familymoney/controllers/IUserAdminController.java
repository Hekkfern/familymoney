package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.dtos.admin.GetUsersResponseDto;
import com.familymoney.familymoney.dtos.user.GetMyUserResponseDto;
import com.familymoney.familymoney.dtos.user.UpdateUserRequestDto;
import com.familymoney.familymoney.types.UserId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/admin/users")
public interface IUserAdminController {

  @GetMapping("/{userId}")
  @NonNull GetMyUserResponseDto getUser(@PathVariable @NotNull UserId userId);

  @GetMapping
  @NonNull GetUsersResponseDto getUsers(
      @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "50") int limit);

  @DeleteMapping("/{userId}")
  void deleteUser(@PathVariable @NotNull UserId userId);

  @PatchMapping("/{userId}")
  void updateUser(
      @PathVariable @NotNull UserId userId, @RequestBody @Valid UpdateUserRequestDto request);

  @PutMapping("/{userId}/role")
  void setUserRole(@PathVariable @NotNull UserId userId, @RequestBody @NotNull String role);

  @GetMapping("/{userId}/role")
  @NonNull String setUserRole(@PathVariable @NotNull UserId userId);
}
