package com.familymoney.domains.admin.controllers;

import com.familymoney.domains.user.controllers.dtos.GetUserResponseDto;
import com.familymoney.domains.user.controllers.dtos.GetUserRoleResponseDto;
import com.familymoney.domains.user.controllers.dtos.UpdateUserRequestDto;
import com.familymoney.testutils.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("admin/users")
public interface IUserAdminController {

  @GetMapping(path = "{userId}", version = "1")
  GetUserResponseDto getUserInfo(@PathVariable @NotNull UUID userId);

  enum SortField {
    CREATED_AT,
    USERNAME,
    EMAIL
  }

  @GetMapping(path = "", version = "1")
  PageResponse<GetUserResponseDto> getUsersInfo(
      @RequestParam(defaultValue = "0") @Min(0) @Max(10_000) int page,
      @RequestParam(defaultValue = "25") @Min(20) @Max(100) int size,
      @RequestParam(defaultValue = "CREATED_AT") SortField sort,
      @RequestParam(defaultValue = "DESC") Sort.Direction direction);

  @GetMapping(path = "total", version = "1")
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
