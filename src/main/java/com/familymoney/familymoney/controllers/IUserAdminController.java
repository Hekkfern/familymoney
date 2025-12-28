package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.controllers.dtos.admin.GetUserResponseDto;
import com.familymoney.familymoney.controllers.dtos.admin.GetUserRoleResponseDto;
import com.familymoney.familymoney.controllers.dtos.admin.GetUsersResponseDto;
import com.familymoney.familymoney.controllers.dtos.user.UpdateUserRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequestMapping("admin/users")
public interface IUserAdminController {

  @GetMapping(path = "{userId}", version = "1")
  GetUserResponseDto getUserInfo(@PathVariable @NotNull UUID userId);

  @GetMapping(path = "", version = "1")
  GetUsersResponseDto getUsersInfo(Pageable pageable);

  @PutMapping(path = "{userId}/enable", version = "1")
  void enableUser(@PathVariable @NotNull UUID userId, @RequestParam boolean enabled);

  @DeleteMapping(path = "{userId}", version = "1")
  void deleteUser(@PathVariable @NotNull UUID userId);

  @PatchMapping(path = "{userId}", version = "1")
  void updateUserInfo(
      @PathVariable @NotNull UUID userId, @RequestBody @Valid UpdateUserRequestDto request);

  @PutMapping(path = "{userId}/role", version = "1")
  void setUserRole(@PathVariable @NotNull UUID userId, @RequestBody @NotBlank String role);

  @GetMapping(path = "{userId}/role", version = "1")
  GetUserRoleResponseDto getUserRole(@PathVariable @NotNull UUID userId);
}
