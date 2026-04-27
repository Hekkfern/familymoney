package com.familymoney.controllers;

import static org.mockito.Mockito.when;

import com.familymoney.config.SecurityConfig;
import com.familymoney.controllers.impl.UserAdminController;
import com.familymoney.controllers.mappers.user.GetMyUserResponseMapper;
import com.familymoney.controllers.mappers.user.GetUserResponseMapper;
import com.familymoney.controllers.mappers.user.UpdateUserRequestMapper;
import com.familymoney.properties.AppProperties;
import com.familymoney.properties.JwtProperties;
import com.familymoney.security.JwtUtils;
import com.familymoney.services.IUserService;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(
    controllers = UserAdminController.class,
    properties = {
      "spring.application.name=testapp",
      "jwt.key=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    })
@Import({
  JwtUtils.class,
  SecurityConfig.class,
  GetUserResponseMapper.class,
  UpdateUserRequestMapper.class
})
@EnableConfigurationProperties({AppProperties.class, JwtProperties.class})
@AutoConfigureRestTestClient
class TransactionGroupControllerTest {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  // region Fields

  @Autowired private RestTestClient client;

  @MockitoSpyBean private JwtUtils jwtUtils;
  @MockitoBean private IUserService userService;
  @MockitoBean private io.jsonwebtoken.Clock jwtClock;
  @MockitoSpyBean private GetMyUserResponseMapper getMyUserResponseMapper;
  @MockitoSpyBean private UpdateUserRequestMapper updateUserRequestMapper;

  // endregion

  @BeforeEach
  void setup() {
    when(jwtClock.now()).thenReturn(Date.from(now));
  }
}
