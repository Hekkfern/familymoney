package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.dtos.user.GetMyUserResponseDto;
import com.familymoney.familymoney.dtos.user.UpdateUserRequestDto;
import jakarta.validation.Valid;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/users")
public interface IUserController {

    @GetMapping("/me")
    @NonNull
    GetMyUserResponseDto getMyUser();

    @DeleteMapping("/me")
    void deleteMyUser();

    @PatchMapping("/me")
    void updateMyUser(@RequestBody @Valid UpdateUserRequestDto request);
}
