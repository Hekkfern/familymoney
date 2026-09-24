package com.familymoney.domains.transactions.controllers;

import static com.familymoney.utils.CustomHttp.IDEMPOTENCY_KEY_HEADER;

import com.familymoney.domains.idempotency.exceptions.IdempotencyConflictException;
import com.familymoney.domains.transactions.controllers.dtos.BalanceDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.EnterGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.GroupDto;
import com.familymoney.domains.transactions.controllers.dtos.InvitationTokenDto;
import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import com.familymoney.domains.transactions.exceptions.GroupInvitationInvalidException;
import com.familymoney.domains.transactions.exceptions.GroupOwnerNotFoundException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupInvitationsReachedException;
import com.familymoney.domains.transactions.exceptions.TransactionGroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
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
   * Creates a group and adds the authenticated user to it as its first member.
   *
   * @param idempotencyKey a client-supplied key that allows the request to be safely retried
   *     without creating a duplicate group
   * @param request the group creation details (name, description, currency)
   * @param httpRequest the underlying HTTP request, used together with {@code idempotencyKey} to
   *     detect a duplicate submission with a different request body
   * @return the identifier of the created group
   * @throws GroupOwnerNotFoundException if the authenticated user cannot be found when it is
   *     assigned as the group's first member
   * @throws IdempotencyConflictException if the idempotency key was already used with a
   *     different request body
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
   * @throws TransactionGroupNotFoundException if no group with the given ID exists
   * @throws UserIsNotMemberOfGroupException if the authenticated user is not a member of the
   *     group
   */
  @Operation(summary = "Delete a group where the authenticated user is a member")
  @DeleteMapping(path = "{groupId}", version = "1")
  void deleteGroup(@PathVariable @NotNull UUID groupId);

  /**
   * Retrieves a group's information where the authenticated user is a member.
   *
   * @param groupId the group identifier
   * @return the group information
   * @throws TransactionGroupNotFoundException if no group with the given ID exists
   * @throws UserIsNotMemberOfGroupException if the authenticated user is not a member of the
   *     group
   */
  @Operation(
      summary = "Get information about a specific group where the authenticated user is a member")
  @GetMapping(path = "{groupId}", version = "1")
  GroupDto getGroupInfo(@PathVariable @NotNull UUID groupId);

  /**
   * Updates a group's information where the authenticated user is a member.
   *
   * @param groupId the group identifier
   * @param request the group fields to update
   * @throws TransactionGroupNotFoundException if no group with the given ID exists
   * @throws UserIsNotMemberOfGroupException if the authenticated user is not a member of the
   *     group
   */
  @Operation(
      summary = "Update information of a specific group where the authenticated user is a member")
  @PatchMapping(path = "{groupId}", version = "1")
  void updateGroupInfo(
      @PathVariable @NotNull UUID groupId, @RequestBody @Valid UpdateGroupRequestDto request);

  /**
   * Retrieves a group's invitation token where the authenticated user is a member. Generates a
   * new token, valid for a limited time, that can be redeemed once through {@link
   * #enterToGroup(EnterGroupRequestDto)}.
   *
   * @param groupId the group identifier
   * @return the invitation token
   * @throws TransactionGroupNotFoundException if no group with the given ID exists
   * @throws UserIsNotMemberOfGroupException if the authenticated user is not a member of the
   *     group
   * @throws MaximumGroupInvitationsReachedException if the group already has the maximum number
   *     of active invitations
   */
  @Operation(
      summary = "Get an invitation token for a group where the authenticated user is a member")
  @GetMapping(path = "{groupId}/invitation", version = "1")
  InvitationTokenDto getInvitationToken(@PathVariable @NotNull UUID groupId);

  /**
   * Adds the authenticated user to a group using an invitation token generated through {@link
   * #getInvitationToken(UUID)}. The token is consumed and can no longer be used once redeemed.
   *
   * @param request the invitation token
   * @throws GroupInvitationInvalidException if the token does not exist or has expired
   */
  @Operation(summary = "Join a group using an invitation token")
  @PostMapping(path = "invitation", version = "1")
  void enterToGroup(@RequestBody @Valid EnterGroupRequestDto request);

  /**
   * Retrieves the list of users in a group where the authenticated user is a member.
   *
   * @param groupId the group identifier
   * @return the identifiers of the group's users
   * @throws TransactionGroupNotFoundException if no group with the given ID exists
   * @throws UserIsNotMemberOfGroupException if the authenticated user is not a member of the
   *     group
   */
  @Operation(summary = "Get the list of users in a specific group")
  @GetMapping(path = "{groupId}/users", version = "1")
  List<UUID> getUsersInGroup(@PathVariable @NotNull UUID groupId);

  /**
   * Removes a user from a group where the authenticated user is a member.
   *
   * @param groupId the group identifier
   * @param userId the identifier of the user to remove
   * @throws TransactionGroupNotFoundException if no group with the given ID exists
   * @throws UserIsNotMemberOfGroupException if the authenticated user is not a member of the
   *     group
   */
  @Operation(
      summary = "Remove a user from a specific group where the authenticated user is a member")
  @DeleteMapping(path = "{groupId}/users/{userId}", version = "1")
  void removeUserFromGroup(@PathVariable @NotNull UUID groupId, @PathVariable @NotNull UUID userId);

  /**
   * Retrieves the balances (debts between members) for a group where the authenticated user is a
   * member. Each entry maps another member of the group to the signed amount owed between that
   * member and the authenticated user.
   *
   * @param groupId the group identifier
   * @return the group balances
   * @throws TransactionGroupNotFoundException if no group with the given ID exists
   * @throws UserIsNotMemberOfGroupException if the authenticated user is not a member of the
   *     group
   */
  @Operation(summary = "Get the balances for a group where the authenticated user is a member")
  @GetMapping(path = "groups/{groupId}/balances", version = "1")
  List<BalanceDto> getGroupBalances(@PathVariable @NotNull UUID groupId);

  /**
   * Retrieves a page of transactions (expenses and payments) for a group where the authenticated
   * user is a member, ordered by completion time, most recent first.
   *
   * @param groupId the group identifier
   * @param page the zero-based index of the page to retrieve
   * @param size the maximum number of transactions to include in the page, between 20 and 100
   * @return a page of transactions for the group
   * @throws TransactionGroupNotFoundException if no group with the given ID exists
   * @throws UserIsNotMemberOfGroupException if the authenticated user is not a member of the
   *     group
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
