package com.familymoney.domains.transactions.controllers;

import com.familymoney.domains.transactions.controllers.dtos.BalanceDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.EnterGroupRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.GroupDto;
import com.familymoney.domains.transactions.controllers.dtos.InvitationTokenDto;
import com.familymoney.domains.transactions.controllers.dtos.MemberDto;
import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import com.familymoney.domains.transactions.exceptions.GroupInvitationInvalidException;
import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.GroupOwnerNotFoundException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupInvitationsReachedException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.utils.PageResponse;
import io.github.benhendayoussef.idempotency.api.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Defines the HTTP API for managing groups and their memberships. */
@RequestMapping("groups")
public interface GroupController {

  /**
   * Creates a group and adds the authenticated user to it as its first member.
   *
   * @param request the group creation details (name, description, currency)
   * @return the ID of the newly created group
   */
  @Operation(summary = "Create a new transaction group")
  @PostMapping(path = "", version = "1")
  @ResponseStatus(HttpStatus.CREATED)
  @Idempotent
  CreateGroupResponseDto createGroupAndAddCreatorAsMember(@RequestBody @Valid CreateGroupRequestDto request);

  /**
   * Retrieves the groups where the authenticated user is a member.
   *
   * @param page the page number to retrieve
   * @param size the number of items per page
   * @return a paginated response of group information
   */
  @Operation(summary = "Retrieves the groups where the authenticated user is a member")
  @GetMapping(path = "", version = "1")
  PageResponse<GroupDto> getGroupsForUser(
      @RequestParam(defaultValue = "0") @Min(0) @Max(100) int page,
      @RequestParam(defaultValue = "20") @Min(20) @Max(100) int size);

  /**
   * Leaves a group where the authenticated user is a member.
   *
   * @param groupId the group identifier
   */
  @Operation(summary = "Leave a group where the authenticated user is a member")
  @DeleteMapping(path = "{groupId}", version = "1")
  void leaveGroup(@PathVariable @NotNull UUID groupId);

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
   * @param request the group fields to update
   */
  @Operation(
      summary = "Update information of a specific group where the authenticated user is a member")
  @PatchMapping(path = "{groupId}", version = "1")
  void updateGroupInfo(
      @PathVariable @NotNull UUID groupId, @RequestBody @Valid UpdateGroupRequestDto request);

  /**
   * Retrieves a group's invitation token where the authenticated user is a member. Generates a new
   * token, valid for a limited time, that can be redeemed once through {@link
   * #enterToGroup(EnterGroupRequestDto)}.
   *
   * @param groupId the group identifier
   * @return the invitation token
   */
  @Operation(
      summary = "Get an invitation token for a group where the authenticated user is a member")
  @PostMapping(path = "{groupId}/invitation", version = "1")
  @Idempotent
  InvitationTokenDto getInvitationToken(@PathVariable @NotNull UUID groupId);

  /**
   * Adds the authenticated user to a group using an invitation token generated through {@link
   * #getInvitationToken(UUID)}. The token is consumed and can no longer be used once redeemed.
   *
   * @param request the invitation token
   */
  @Operation(summary = "Join a group using an invitation token")
  @PostMapping(path = "invitation", version = "1")
  @Idempotent
  void enterToGroup(@RequestBody @Valid EnterGroupRequestDto request);

  /**
   * Retrieves the list of users in a group where the authenticated user is a member.
   *
   * @param groupId the group identifier
   * @return the group's users
   */
  @Operation(summary = "Get the list of users in a specific group")
  @GetMapping(path = "{groupId}/users", version = "1")
  List<MemberDto> getUsersInGroup(@PathVariable @NotNull UUID groupId);

  /**
   * Retrieves the balances (debts between members) for a group where the authenticated user is a
   * member. Each entry maps another member of the group to the signed amount owed between that
   * member and the authenticated user.
   *
   * @param groupId the group identifier
   * @return the group balances
   */
  @Operation(summary = "Get the balances for a group where the authenticated user is a member")
  @GetMapping(path = "{groupId}/balances", version = "1")
  List<BalanceDto> getGroupBalances(@PathVariable @NotNull UUID groupId);

  /**
   * Retrieves a page of transactions (expenses and payments) for a group where the authenticated
   * user is a member, ordered by completion time, most recent first.
   *
   * @param groupId the group identifier
   * @param page the zero-based index of the page to retrieve
   * @param size the maximum number of transactions to include in the page, between 20 and 100
   * @return a page of transactions for the group
   */
  @Operation(
      summary =
          "Get the transactions (expenses and payments) for a group where the authenticated user is a member")
  @GetMapping(path = "{groupId}/transactions", version = "1")
  PageResponse<TransactionDto> getGroupTransactions(
      @PathVariable @NotNull UUID groupId,
      @RequestParam(defaultValue = "0") @Min(0) @Max(10_000) int page,
      @RequestParam(defaultValue = "20") @Min(20) @Max(100) int size);
}
