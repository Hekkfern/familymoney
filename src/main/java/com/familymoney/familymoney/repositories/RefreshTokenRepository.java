package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.RefreshTokenDbo;
import com.familymoney.familymoney.repositories.rowmappers.RefreshTokenRowMapper;
import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository implements IRefreshTokenRepository {

  private final JdbcClient jdbcClient;

  @Override
  @NonNull
  public Optional<RefreshTokenDbo> create(
      @NonNull UserId userId, @NonNull RefreshToken token, @NonNull UUID family) {
    var sql =
        """
                INSERT INTO refresh_tokens (user_id, token, family)
                VALUES (:userId, :token, :family)
                RETURNING id, user_id, token, created_at, expires_at, is_used, used_at, family
                """;
    return jdbcClient
        .sql(sql)
        .param("userId", userId.toString())
        .param("token", token.toString())
        .param("family", family)
        .query(new RefreshTokenRowMapper())
        .optional();
  }

  @Override
  @NonNull
  public Optional<RefreshTokenDbo> findByToken(@NonNull RefreshToken token) {
    var sql =
        """
                SELECT id, user_id, token, created_at, expires_at, is_used, used_at, family
                FROM refresh_tokens
                WHERE token = :token
                """;
    return jdbcClient
        .sql(sql)
        .param("token", token.toString())
        .query(new RefreshTokenRowMapper())
        .optional();
  }

  @Override
  public void markTokenAsUsed(@NonNull RefreshToken token) {
    var sql =
        """
                UPDATE refresh_tokens
                SET is_used = TRUE,
                    used_at = NOW()
                WHERE token = :token
                """;
    jdbcClient.sql(sql).param("token", token.toString()).update();
  }

  @Override
  public void invalidateByFamily(@NonNull UUID family) {
    var sql =
        """
                UPDATE refresh_tokens
                SET is_used = TRUE
                WHERE family = :family
                """;
    jdbcClient.sql(sql).param("family", family).update();
  }

  @Override
  public void invalidateByUserId(@NonNull UserId userId) {
    var sql =
        """
                UPDATE refresh_tokens
                SET is_used = TRUE
                WHERE user_id = :userId
                """;
    jdbcClient.sql(sql).param("userId", userId.toString()).update();
  }

  @Override
  public void deleteOlderThan(@NonNull Duration cutoff) {
    var sql =
        """
                DELETE FROM refresh_tokens
                WHERE created_at < NOW() - INTERVAL ':duration seconds'
                """;
    jdbcClient.sql(sql).param("duration", cutoff.toSeconds()).update();
  }
}
