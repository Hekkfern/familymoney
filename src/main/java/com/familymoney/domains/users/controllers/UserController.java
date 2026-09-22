package com.familymoney.domains.users.controllers;

import com.familymoney.domains.users.controllers.dtos.UpdateUserRequestDto;
import com.familymoney.domains.users.controllers.dtos.UserDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("users")
public interface UserController {

  @GetMapping(path = "me", version = "1")
  UserDto getMyUserInfo();

  @DeleteMapping(path = "me", version = "1")
  void deleteMyUser();

  @PatchMapping(path = "me", version = "1")
  void updateMyUserInfo(@RequestBody @Valid UpdateUserRequestDto request);
}
