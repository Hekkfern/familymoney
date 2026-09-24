package com.familymoney.domains.transactions.controllers;

import static com.familymoney.utils.CustomHttp.IDEMPOTENCY_KEY_HEADER;

import com.familymoney.domains.transactions.controllers.dtos.BalanceDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.EnterGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.GroupDto;
import com.familymoney.domains.transactions.controllers.dtos.InvitationTokenDto;
import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import com.familymoney.utils.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Defines the HTTP API for managing groups and their memberships. */
@RequestMapping("groups")
public interface GroupController {

  /**
   * Creates a group.
   *
   * @param request the group creation details
   * @return the identifier of the created group
   */
  @Operation(summary = "Create a new transaction group")
  @PostMapping(path = "", version = "1")
  CreateGroupResponseDto createGroup(
      @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @RequestBody @Valid CreateGroupRequestDto request,
      HttpServletRequest httpRequest);

  /**
   * Retrieves the ID of the groups where the authenticated user is a member.
   *
   * @return a list of group identifiers
   */
  @Operation(summary = "Retrieves the ID of the groups where the authenticated user is a member")
  @GetMapping(path = "", version = "1")
  List<UUID> getGroupsForUser();

  /**
   * Deletes a group where the authenticated user is a member.
   *
   * @param groupId the group identifier
   */
  @Operation(summary = "Delete a group where the authenticated user is a member")
  @DeleteMapping(path = "{groupId}", version = "1")
  void deleteGroup(@PathVariable @NotNull UUID groupId);

  /**
   * Retrieves a group's information where the authenticated user is a member.
   *
   * @param groupId the group identifier
   * @return the group information
   */
  @Operation(
      summary = "Get information about a specific group where the authenticated user is a member")
  @GetMapping(path = "{groupId}", version = "1")
  GroupDto getGroupInfo(@PathVariable @NotNull UUID groupId);

  /**
   * Updates a group's information where the authenticated user is a member.
   *
   * @param groupId the group identifier
   * @param request the group updates
   */
  @Operation(
      summary = "Update information of a specific group where the authenticated user is a member")
  @PatchMapping(path = "{groupId}", version = "1")
  void updateGroupInfo(
      @PathVariable @NotNull UUID groupId, @RequestBody @Valid UpdateGroupRequestDto request);

  /**
   * Retrieves a group's invitation token where the authenticated user is a member.
   *
   * @param groupId the group identifier
   * @return the invitation token
   */
  @Operation(
      summary = "Get an invitation token for a group where the authenticated user is a member")
  @GetMapping(path = "{groupId}/invitation", version = "1")
  InvitationTokenDto getInvitationToken(@PathVariable @NotNull UUID groupId);

  /**
   * Adds the authenticated user to a group using an invitation token.
   *
   * @param request the invitation token
   */
  @Operation(summary = "Join a group using an invitation token")
  @PostMapping(path = "invitation", version = "1")
  void enterToGroup(@RequestBody @Valid EnterGroupRequestDto request);

  /**
   * Retrieves the list of users in a group where the authenticated user is a member.
   *
   * @param groupId the group identifier
   * @return the group users
   */
  @Operation(summary = "Get the list of users in a specific group")
  @GetMapping(path = "{groupId}/users", version = "1")
  List<UUID> getUsersInGroup(@PathVariable @NotNull UUID groupId);

  /**
   * Removes a user from a group where the authenticated user is a member.
   *
   * @param groupId the group identifier
   * @param userId the user identifier
   */
  @Operation(
      summary = "Remove a user from a specific group where the authenticated user is a member")
  @DeleteMapping(path = "{groupId}/users/{userId}", version = "1")
  void removeUserFromGroup(@PathVariable @NotNull UUID groupId, @PathVariable @NotNull UUID userId);

  /**
   * Retrieves the balances (debts between members) for a group where the authenticated user is a
   * member.
   *
   * @param groupId the group identifier
   * @return the group balances
   */
  @Operation(summary = "Get the balances for a group where the authenticated user is a member")
  @GetMapping(path = "groups/{groupId}/balances", version = "1")
  List<BalanceDto> getGroupBalances(@PathVariable @NotNull UUID groupId);

  /**
   * Retrieves the transactions (expenses and payments) for a group where the authenticated user is
   * a member.
   *
   * @param groupId the group identifier
   * @return the group transactions
   */
  @Operation(
      summary =
          "Get the transactions (expenses and payments) for a group where the authenticated user is a member")
  @GetMapping(path = "groups/{groupId}/transactions", version = "1")
  PageResponse<TransactionDto> getGroupTransactions(
      @PathVariable @NotNull UUID groupId,
      @RequestParam(defaultValue = "0") @Min(0) @Max(10_000) int page,
      @RequestParam(defaultValue = "20") @Min(20) @Max(100) int size);
}
