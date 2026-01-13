package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.EmailVerificationDbo;
import com.familymoney.familymoney.repositories.mappers.EmailVerificationRowMapper;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepository implements IEmailVerificationRepository {

  private final JdbcClient jdbcClient;

  @Override
  public Optional<EmailVerificationDbo> create(
      final UserId userId, final EmailVerificationToken token, final Instant expiresAt) {
    val sql =
        """
        INSERT INTO email_verification_tokens (user_id, token, expires_at)
        VALUES (:userId, :token, :expiresAt)
        RETURNING id, user_id, token, expires_at, created_at
        """;
    return jdbcClient
        .sql(sql)
        .param("userId", userId.value())
        .param("token", token.value())
        .param("expiresAt", OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC))
        .query(new EmailVerificationRowMapper())
        .optional();
  }

  @Override
  public Optional<EmailVerificationDbo> findByToken(final EmailVerificationToken token) {
    val sql =
        """
        SELECT id, user_id, token, expires_at, created_at
        FROM email_verification_tokens
        WHERE token = :token
        """;
    return jdbcClient
        .sql(sql)
        .param("token", token.value())
        .query(new EmailVerificationRowMapper())
        .optional();
  }

  @Override
  public boolean deleteByUserId(final UserId userId) {
    val sql =
        """
        DELETE FROM email_verification_tokens
        WHERE user_id = :userId
        """;
    val rowsAffected = jdbcClient.sql(sql).param("userId", userId.value()).update();
    return rowsAffected > 0;
  }

  @Override
  public void deleteOlderThan(final Duration cutoff) {
    val sql =
        """
        DELETE FROM email_verification_tokens
        WHERE created_at < NOW() - INTERVAL ':duration seconds'
        """;
    jdbcClient.sql(sql).param("duration", cutoff.getSeconds()).update();
  }
}
