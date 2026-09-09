package com.familymoney.domains.transactions.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.familymoney.domains.transactions.services.TransactionService;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.security.JwtAuthFilter;
import com.familymoney.testutils.GroupControllerUriFactory;
import com.familymoney.testutils.WithMockUserId;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(
    controllers = TransactionController.class,
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestTestClient
class DefaultTransactionControllerTest {

  private static final String USER_ID = "019d52d0-d1b8-7d2d-ba2d-39007c0dda4f";
  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  @Autowired private RestTestClient client;

  @MockitoBean private Clock clock;
  @MockitoBean private TransactionService transactionService;

  @BeforeEach
  void setup() {
    when(clock.instant()).thenReturn(now);
  }

  @Nested
  class GroupBalances {

    @Test
    @WithMockUserId(userId = USER_ID)
    void returns_group_balances() {
      when(transactionService.getAllGroupBalances(any(), any()))
          .thenReturn(
              Map.of(UserId.generate(), Money.of(BigDecimal.TEN, Monetary.getCurrency("USD"))));

      client
          .get()
          .uri(GroupControllerUriFactory.getBalancesPath(GroupId.generate().toString()))
          .exchange()
          .expectStatus()
          .isOk();
    }

    @Test
    void bad_request_when_group_id_is_invalid_for_balances() {
      client
          .get()
          .uri(GroupControllerUriFactory.getBalancesPath("invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionService);
    }
  }
}
