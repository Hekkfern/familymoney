package com.familymoney.familymoney.dtos.user;

public record UpdateUserRequestDto(
        String username,
        String email,
        String password) {

}
