package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.RefreshTokenDbo;
import com.familymoney.familymoney.repositories.dbos.UpdateRefreshTokenDbo;
import com.familymoney.familymoney.repositories.mappers.RefreshTokenRowMapper;
import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository implements IRefreshTokenRepository {

  private final JdbcClient jdbcClient;

  @Override
  public Optional<RefreshTokenDbo> create(UserId userId, RefreshToken token, UUID family) {
    val sql =
        """
        INSERT INTO refresh_tokens (user_id, token, family)
        VALUES (:userId, :token, :family)
        RETURNING id, user_id, token, created_at, expires_at, is_used, used_at, family
        """;
    return jdbcClient
        .sql(sql)
        .param("userId", userId.value())
        .param("token", token.value())
        .param("family", family)
        .query(new RefreshTokenRowMapper())
        .optional();
  }

  @Override
  public Optional<RefreshTokenDbo> findByToken(RefreshToken token) {
    val sql =
        """
        SELECT id, user_id, token, created_at, expires_at, is_used, used_at, family
        FROM refresh_tokens
        WHERE token = :token
        """;
    return jdbcClient
        .sql(sql)
        .param("token", token.value())
        .query(new RefreshTokenRowMapper())
        .optional();
  }

  @Override
  public boolean updateByToken(RefreshToken token, UpdateRefreshTokenDbo data) {
    val sql =
        """
        UPDATE users
        SET is_used = COALESCE(:isUsed, is_used),
            used_at = COALESCE(:usedAt, used_at)
        WHERE token = :token
        """;
    val rowsAffected =
        jdbcClient
            .sql(sql)
            .param("token", token.value())
            .param("isUsed", data.getIsUsed())
            .param(
                "usedAt",
                data.getUsedAt() != null
                    ? OffsetDateTime.ofInstant(data.getUsedAt(), ZoneOffset.UTC)
                    : null)
            .update();
    return rowsAffected > 0;
  }

  @Override
  public boolean updateByFamily(UUID family, UpdateRefreshTokenDbo data) {
    val sql =
        """
        UPDATE users
        SET is_used = COALESCE(:isUsed, is_used),
            used_at = COALESCE(:usedAt, used_at)
        WHERE family = :family
        """;
    val rowsAffected =
        jdbcClient
            .sql(sql)
            .param("family", family)
            .param("isUsed", data.getIsUsed())
            .param(
                "usedAt",
                data.getUsedAt() != null
                    ? OffsetDateTime.ofInstant(data.getUsedAt(), ZoneOffset.UTC)
                    : null)
            .update();
    return rowsAffected > 0;
  }

  @Override
  public boolean updateByUserId(UserId userId, UpdateRefreshTokenDbo data) {
    val sql =
        """
        UPDATE users
        SET is_used = COALESCE(:isUsed, is_used),
            used_at = COALESCE(:usedAt, used_at)
        WHERE user_id = :userId
        """;
    val rowsAffected =
        jdbcClient
            .sql(sql)
            .param("userId", userId.value())
            .param("isUsed", data.getIsUsed())
            .param(
                "usedAt",
                data.getUsedAt() != null
                    ? OffsetDateTime.ofInstant(data.getUsedAt(), ZoneOffset.UTC)
                    : null)
            .update();
    return rowsAffected > 0;
  }

  @Override
  public void deleteOlderThan(Duration cutoff) {
    val sql =
        """
        DELETE FROM refresh_tokens
        WHERE created_at < NOW() - INTERVAL ':duration seconds'
        """;
    jdbcClient.sql(sql).param("duration", cutoff.toSeconds()).update();
  }
}
