package com.familymoney.domains.transactions.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.familymoney.config.ApiConfig;
import com.familymoney.domains.transactions.exceptions.GroupExceptionHandler;
import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.services.GroupService;
import com.familymoney.domains.transactions.services.data.ExpenseData;
import com.familymoney.domains.transactions.services.data.PaymentData;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.testutils.WithMockUserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(DefaultGroupController.class)
@ContextConfiguration(
    classes = {DefaultGroupController.class, ApiConfig.class, GroupExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestTestClient
class GroupTransactionsControllerTest {

  private static final String USER_ID = "00000000-0000-0000-0000-000000000001";
  private static final Instant DONE_AT = Instant.parse("2026-01-01T12:00:00Z");
  private final GroupId groupId = GroupId.generate();
  @Autowired private RestTestClient client;
  @MockitoBean private GroupService groupService;

  @Nested
  class GetGroupTransactions {

    @Test
    @WithMockUserId(userId = USER_ID)
    void returns_complete_mixed_subtypes_and_pagination_metadata() {
      final ExpenseId expenseId = ExpenseId.generate();
      final PaymentId paymentId = PaymentId.generate();
      final UserId sender = UserId.fromString(USER_ID);
      final UserId recipient = UserId.generate();
      final TransactionData expense =
          new TransactionData.Expense(
              new ExpenseData(
                  expenseId,
                  groupId,
                  Description.of("Dinner"),
                  Money.of(new BigDecimal("12.345"), "USD"),
                  DONE_AT,
                  Map.of(recipient, new BigDecimal("12.345")),
                  Map.of(sender, new BigDecimal("12.345"))));
      final TransactionData payment =
          new TransactionData.Payment(
              new PaymentData(
                  paymentId,
                  groupId,
                  Description.of("Settlement"),
                  Money.of(new BigDecimal("7.125"), "USD"),
                  DONE_AT.minusSeconds(1),
                  sender,
                  recipient));
      when(groupService.getGroupTransactions(eq(groupId), any(), eq(1), eq(20)))
          .thenReturn(new PageImpl<>(List.of(expense, payment), PageRequest.of(1, 20), 22));

      client
          .get()
          .uri(path() + "?page=1&size=20")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.page")
          .isEqualTo(1)
          .jsonPath("$.size")
          .isEqualTo(20)
          .jsonPath("$.totalElements")
          .isEqualTo(22)
          .jsonPath("$.totalPages")
          .isEqualTo(2)
          .jsonPath("$.first")
          .isEqualTo(false)
          .jsonPath("$.last")
          .isEqualTo(true)
          .jsonPath("$.content.length()")
          .isEqualTo(2)
          .jsonPath("$.content[0].type")
          .isEqualTo("EXPENSE")
          .jsonPath("$.content[0].id")
          .isEqualTo(expenseId.toString())
          .jsonPath("$.content[0].amount")
          .isEqualTo(12.345)
          .jsonPath("$.content[0].description")
          .isEqualTo("Dinner")
          .jsonPath("$.content[0].doneAt")
          .isEqualTo(DONE_AT.toString())
          .jsonPath("$.content[0].payers[0]")
          .isEqualTo(USER_ID)
          .jsonPath("$.content[0].shares[0]")
          .isEqualTo(recipient.toString())
          .jsonPath("$.content[0].fromUser")
          .doesNotExist()
          .jsonPath("$.content[0].toUser")
          .doesNotExist()
          .jsonPath("$.content[1].type")
          .isEqualTo("PAYMENT")
          .jsonPath("$.content[1].id")
          .isEqualTo(paymentId.toString())
          .jsonPath("$.content[1].amount")
          .isEqualTo(7.125)
          .jsonPath("$.content[1].description")
          .isEqualTo("Settlement")
          .jsonPath("$.content[1].doneAt")
          .isEqualTo(DONE_AT.minusSeconds(1).toString())
          .jsonPath("$.content[1].fromUser")
          .isEqualTo(USER_ID)
          .jsonPath("$.content[1].toUser")
          .isEqualTo(recipient.toString())
          .jsonPath("$.content[1].payers")
          .doesNotExist()
          .jsonPath("$.content[1].shares")
          .doesNotExist();
    }

    @Test
    @WithMockUserId(userId = USER_ID)
    void applies_default_pagination_and_returns_an_empty_page() {
      when(groupService.getGroupTransactions(eq(groupId), any(), eq(0), eq(20)))
          .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

      client
          .get()
          .uri(path())
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.content")
          .isEmpty()
          .jsonPath("$.page")
          .isEqualTo(0)
          .jsonPath("$.size")
          .isEqualTo(20)
          .jsonPath("$.totalElements")
          .isEqualTo(0);
      verify(groupService).getGroupTransactions(eq(groupId), any(), eq(0), eq(20));
    }

    @ParameterizedTest
    @CsvSource({"-1,20", "10001,20", "0,19", "0,101"})
    @WithMockUserId(userId = USER_ID)
    void rejects_invalid_pagination(final int page, final int size) {
      client
          .get()
          .uri(path() + "?page=" + page + "&size=" + size)
          .exchange()
          .expectStatus()
          .isBadRequest();
      verifyNoInteractions(groupService);
    }

    @Test
    @WithMockUserId(userId = USER_ID)
    void missing_group_returns_not_found() {
      when(groupService.getGroupTransactions(eq(groupId), any(), eq(0), eq(20)))
          .thenThrow(new GroupNotFoundException("Missing group"));

      client.get().uri(path()).exchange().expectStatus().isNotFound();
    }

    @Test
    @WithMockUserId(userId = USER_ID)
    void nonmember_retains_the_existing_unauthorized_response() {
      when(groupService.getGroupTransactions(eq(groupId), any(), eq(0), eq(20)))
          .thenThrow(new UserIsNotMemberOfGroupException("Not a member"));

      client.get().uri(path()).exchange().expectStatus().isUnauthorized();
    }
  }

  private String path() {
    return "/api/v1/groups/" + groupId.value() + "/transactions";
  }
}
