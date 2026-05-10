package com.familymoney.domains.user.controllers;

import com.familymoney.domains.user.controllers.dtos.GetMyUserResponseDto;
import com.familymoney.domains.user.controllers.dtos.UpdateUserRequestDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("users")
public interface IUserController {

  @GetMapping(path = "me", version = "1")
  GetMyUserResponseDto getMyUserInfo();

  @DeleteMapping(path = "me", version = "1")
  void deleteMyUser();

  @PatchMapping(path = "me", version = "1")
  void updateMyUserInfo(@RequestBody @Valid UpdateUserRequestDto request);
}
