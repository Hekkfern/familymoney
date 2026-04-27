package com.familymoney.controllers;

import com.familymoney.controllers.dtos.auth.ForgotPasswordRequestDto;
import com.familymoney.controllers.dtos.auth.LoginRequestDto;
import com.familymoney.controllers.dtos.auth.LoginResponseDto;
import com.familymoney.controllers.dtos.auth.LogoutRequestDto;
import com.familymoney.controllers.dtos.auth.RefreshResponseDto;
import com.familymoney.controllers.dtos.auth.RefreshTokenRequestDto;
import com.familymoney.controllers.dtos.auth.RegisterRequestDto;
import com.familymoney.controllers.dtos.auth.ResendVerificationEmailRequestDto;
import com.familymoney.controllers.dtos.auth.ResetPasswordRequestDto;
import com.familymoney.validation.ValidEmailVerificationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(
    name = "Registration and Authentication APIs",
    description = "Operations to register a new user and authenticate")
@RequestMapping("auth")
public interface IAuthController {

  @Operation(summary = "Register a new user account")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Registration request data",
      content =
          @Content(
              examples =
                  @ExampleObject(
                      value =
                          "{\"username\":\"hectorfern\",\"email\":\"hector@mail.com\",\"password\":\"P@ssw0rd!123456\"}")))
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "User account registered successfully"),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request data",
        content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
    @ApiResponse(
        responseCode = "409",
        description = "Username or email already exists",
        content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
    @ApiResponse(
        responseCode = "500",
        description = "Something went wrong on the server side",
        content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
  })
  @PostMapping(path = "register", version = "1")
  void register(@RequestBody @Valid RegisterRequestDto request);

  @Operation(summary = "Log in using an existing user account")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "User logged in successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = {
                  @ExampleObject(
                      value =
                          "{\"accessToken\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30\",\"refreshToken\":\"046460f4397d444faf489609b411567b\"}")
                })),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request data",
        content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
    @ApiResponse(
        responseCode = "401",
        description = "Invalid user credentials",
        content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
    @ApiResponse(
        responseCode = "500",
        description = "Something went wrong on the server side",
        content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
  })
  @PostMapping(path = "login", version = "1")
  LoginResponseDto login(@RequestBody @Valid LoginRequestDto request);

  @Operation(summary = "Verify the email address of an user account using the verification token")
  @GetMapping(path = "verify-email/{token}", version = "1")
  void verifyEmail(@PathVariable @NotNull @ValidEmailVerificationToken String token);

  @Operation(summary = "Resend the email verification email to the user")
  @PostMapping(path = "verify-email/resend", version = "1")
  void resendVerificationEmail(@RequestBody @Valid ResendVerificationEmailRequestDto request);

  @Operation(summary = "Initiate the password reset process for a user account")
  @PostMapping(path = "forgot-password", version = "1")
  void forgotPassword(@RequestBody @Valid ForgotPasswordRequestDto request);

  @Operation(summary = "Reset the password of a user account using the password reset token")
  @PostMapping(path = "reset-password", version = "1")
  void resetPassword(@RequestBody @Valid ResetPasswordRequestDto request);

  @Operation(summary = "Refresh the access and refresh tokens using a valid refresh token")
  @PostMapping(path = "refresh", version = "1")
  RefreshResponseDto refresh(@RequestBody @Valid RefreshTokenRequestDto request);

  @Operation(summary = "Log out the user and invalidate the refresh token")
  @PostMapping(path = "logout", version = "1")
  void logout(@RequestBody @Valid LogoutRequestDto request);
}
