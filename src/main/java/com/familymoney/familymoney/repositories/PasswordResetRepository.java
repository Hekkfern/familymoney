package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.PasswordResetDbo;
import com.familymoney.familymoney.repositories.mappers.PasswordResetRowMapper;
import com.familymoney.familymoney.types.PasswordResetToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PasswordResetRepository implements IPasswordResetRepository {

  private final JdbcClient jdbcClient;

  @Override
  public Optional<PasswordResetDbo> create(
      final UserId userId, final PasswordResetToken token, final Instant expiresAt) {
    var sql =
        """
        INSERT INTO password_reset_tokens (user_id, token, expires_at)
        VALUES (:userId, :token, :expiresAt)
        RETURNING id, user_id, token, expires_at, created_at
        """;
    return jdbcClient
        .sql(sql)
        .param("userId", userId.value())
        .param("token", token.value())
        .param("expiresAt", OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC))
        .query(new PasswordResetRowMapper())
        .optional();
  }

  @Override
  public Optional<PasswordResetDbo> findByToken(final PasswordResetToken token) {
    // TODO
    return Optional.empty();
  }

  @Override
  public boolean deleteByUserId(final UserId userId) {
    // TODO
    return true;
  }
}
