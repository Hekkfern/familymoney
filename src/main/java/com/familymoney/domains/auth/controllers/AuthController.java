package com.familymoney.domains.auth.controllers;

import com.familymoney.domains.auth.controllers.dtos.ForgotPasswordRequestDto;
import com.familymoney.domains.auth.controllers.dtos.LoginRequestDto;
import com.familymoney.domains.auth.controllers.dtos.LoginResponseDto;
import com.familymoney.domains.auth.controllers.dtos.LogoutRequestDto;
import com.familymoney.domains.auth.controllers.dtos.RefreshResponseDto;
import com.familymoney.domains.auth.controllers.dtos.RefreshTokenRequestDto;
import com.familymoney.domains.auth.controllers.dtos.RegisterRequestDto;
import com.familymoney.domains.auth.controllers.dtos.ResendVerificationEmailRequestDto;
import com.familymoney.domains.auth.controllers.dtos.ResetPasswordRequestDto;
import com.familymoney.domains.auth.controllers.dtos.VerifyEmailRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(
    name = "Registration and Authentication APIs",
    description = "Operations to register a new user and authenticate")
@RequestMapping("auth")
public interface AuthController {

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
  @PostMapping(path = "verify-email", version = "1")
  void verifyEmail(@RequestBody @Valid VerifyEmailRequestDto request);

  @Operation(summary = "Resend the email verification email to the user")
  @ApiResponses({
    @ApiResponse(responseCode = "202", description = "Verification email request accepted"),
    @ApiResponse(responseCode = "429", description = "Verification email request rate limited")
  })
  @PostMapping(path = "verify-email/resend", version = "1")
  @ResponseStatus(HttpStatus.ACCEPTED)
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
