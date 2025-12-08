package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.repositories.mappers.UserDboRowMapper;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.Username;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class UserRepository implements IUserRepository {

  private final JdbcClient jdbcClient;

  @Override
  @NonNull
  public Optional<UserDbo> create(
      @NonNull Username username, @NonNull Email email, @NonNull String passwordHash) {
    val sql =
        """
        INSERT INTO users (username, email, hashed_password)
        VALUES (:username, :email, :passwordHash)
        RETURNING id, username, email, hashed_password, email_verified, created_at, updated_at, is_enabled
        """;
    return jdbcClient
        .sql(sql)
        .param("username", username.value())
        .param("email", email.value())
        .param("passwordHash", passwordHash)
        .query(new UserDboRowMapper())
        .optional();
  }

  @Override
  @NonNull
  public Optional<UserDbo> findById(@NonNull UserId id) {
    val sql =
        """
        SELECT id, username, email, hashed_password, created_at, updated_at, email_verified, is_enabled
        FROM users
        WHERE id = :id
        """;
    return jdbcClient.sql(sql).param("id", id.value()).query(new UserDboRowMapper()).optional();
  }

  @Override
  @NonNull
  public Optional<UserDbo> findByEmail(@NonNull Email email) {
    val sql =
        """
        SELECT id, username, email, hashed_password, created_at, updated_at, email_verified, is_enabled
        FROM users
        WHERE email = :email
        """;
    return jdbcClient
        .sql(sql)
        .param("email", email.value())
        .query(new UserDboRowMapper())
        .optional();
  }

  @Override
  @NonNull
  public Optional<UserDbo> findByUsername(@NonNull Username username) {
    val sql =
        """
        SELECT id, username, email, hashed_password, created_at, updated_at, email_verified, is_enabled
        FROM users
        WHERE username = :username
        """;
    return jdbcClient
        .sql(sql)
        .param("username", username.value())
        .query(new UserDboRowMapper())
        .optional();
  }

  @Override
  public boolean existsByEmailOrUsername(@NonNull Email email, @NonNull Username username) {
    var sql =
        """
        SELECT COUNT(*)
        FROM users
        WHERE email = :email OR username = :username
        """;
    val count =
        jdbcClient
            .sql(sql)
            .param("email", email.value())
            .param("username", username.value())
            .query(Integer.class)
            .single();
    return count > 0;
  }

  @Override
  public void updateInfo(
      @NonNull UserId id, @NonNull Optional<Username> username, @NonNull Optional<Email> email) {
    val sql =
        """
        UPDATE users
        SET username = COALESCE(:username, username),
            email = COALESCE(:email, email)
        WHERE id = :id
        """;
    jdbcClient
        .sql(sql)
        .param("id", id.value())
        .param("username", username.map(Username::value).orElse(null))
        .param("email", email.map(Email::value).orElse(null))
        .update();
  }

  @Override
  public void updatePassword(@NonNull UserId id, @NonNull String newPasswordHash) {
    val sql =
        """
        UPDATE users
        SET hashed_password = :passwordHash
        WHERE id = :id
        """;
    jdbcClient.sql(sql).param("id", id.value()).param("passwordHash", newPasswordHash).update();
  }

  @Override
  public void deleteById(@NonNull UserId id) {
    val sql =
        """
        DELETE FROM users
        WHERE id = :id
        """;
    jdbcClient.sql(sql).param("id", id.value()).update();
  }

  @Override
  public void deleteByIsUnverifiedAndOlderThan(@NonNull Duration cutoff) {
    val sql =
        """
        DELETE FROM users
        WHERE email_verified = FALSE
          AND created_at < NOW() - INTERVAL ':duration seconds'
        """;
    jdbcClient.sql(sql).params("duration", cutoff).update();
  }

  @Override
  public void setIsEnabledByUserId(@NonNull UserId id, boolean isEnabled) {
    val sql =
        """
        UPDATE users
        SET is_enabled = :isEnabled
        WHERE id = :id
        """;
    jdbcClient.sql(sql).param("id", id.value()).param("isEnabled", isEnabled).update();
  }

  @Transactional
  @Override
  public @NonNull Page<@NonNull UserDbo> findAll(Pageable pageable) {
    val rowCountSql =
        """
          SELECT COUNT(1)
          FROM users
          """;
    val total = jdbcClient.sql(rowCountSql).query(Long.class).single();
    val querySql =
        """
        SELECT id, username, email, hashed_password, created_at, updated_at, email_verified, is_enabled
        FROM users
        LIMIT :limit
        OFFSET :offset
        """;
    val data =
        jdbcClient
            .sql(querySql)
            .param("limit", pageable.getPageSize())
            .param("offset", pageable.getOffset())
            .query(new UserDboRowMapper())
            .list();
    return new PageImpl<>(data, pageable, total);
  }
}
