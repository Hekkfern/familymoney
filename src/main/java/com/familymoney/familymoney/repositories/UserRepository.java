package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.UpdateUserDbo;
import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.repositories.mappers.UserDboRowMapper;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.UserName;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
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
  public Optional<UserDbo> create(UserName username, Email email, String passwordHash) {
    val sql =
        """
        INSERT INTO users (username, email, hashed_password)
        VALUES (:username, :email, :passwordHash)
        RETURNING id, username, email, hashed_password, email_verified, created_at, updated_at, is_enabled
        """;
    return jdbcClient
        .sql(sql)
        .param("username", username.toString())
        .param("email", email.toString())
        .param("passwordHash", passwordHash)
        .query(new UserDboRowMapper())
        .optional();
  }

  @Override
  public Optional<UserDbo> findById(UserId id) {
    val sql =
        """
        SELECT id, username, email, hashed_password, created_at, updated_at, email_verified, is_enabled
        FROM users
        WHERE id = :id
        """;
    return jdbcClient.sql(sql).param("id", id.toString()).query(new UserDboRowMapper()).optional();
  }

  @Override
  public Optional<UserDbo> findByEmail(Email email) {
    val sql =
        """
        SELECT id, username, email, hashed_password, created_at, updated_at, email_verified, is_enabled
        FROM users
        WHERE email = :email
        """;
    return jdbcClient
        .sql(sql)
        .param("email", email.toString())
        .query(new UserDboRowMapper())
        .optional();
  }

  @Override
  public Optional<UserDbo> findByUsername(UserName username) {
    val sql =
        """
        SELECT id, username, email, hashed_password, created_at, updated_at, email_verified, is_enabled
        FROM users
        WHERE username = :username
        """;
    return jdbcClient
        .sql(sql)
        .param("username", username.toString())
        .query(new UserDboRowMapper())
        .optional();
  }

  @Override
  public boolean existsByEmailOrUsername(Email email, UserName username) {
    val sql =
        """
        SELECT COUNT(*)
        FROM users
        WHERE email = :email OR username = :username
        """;
    val count =
        jdbcClient
            .sql(sql)
            .param("email", email.toString())
            .param("username", username.toString())
            .query(Integer.class)
            .single();
    return count > 0;
  }

  @Override
  public boolean updateById(UserId id, UpdateUserDbo data) {
    val sql =
        """
        UPDATE users
        SET username = COALESCE(:username, username),
            email = COALESCE(:email, email)
            hashed_password = COALESCE(:hashedPassword, hashed_password)
            is_email_verified = COALESCE(:isEmailVerified, is_email_verified)
            is_enabled = COALESCE(:isEnabled, is_enabled)
        WHERE id = :id
        """;
    val rowsAffected =
        jdbcClient
            .sql(sql)
            .param("id", id.toString())
            .param("username", data.getUsername() != null ? data.getUsername().toString() : null)
            .param("email", data.getEmail() != null ? data.getEmail().toString() : null)
            .param("hashedPassword", data.getHashedPassword())
            .param("isEmailVerified", data.getIsEmailVerified())
            .param("isEnabled", data.getIsEnabled())
            .update();
    return rowsAffected > 0;
  }

  @Override
  public boolean deleteById(UserId id) {
    val sql =
        """
        DELETE FROM users
        WHERE id = :id
        """;
    val rowsAffected = jdbcClient.sql(sql).param("id", id.toString()).update();
    return rowsAffected > 0;
  }

  @Override
  public void deleteByIsUnverifiedAndOlderThan(Duration cutoff) {
    val sql =
        """
        DELETE FROM users
        WHERE email_verified = FALSE
          AND created_at < NOW() - INTERVAL ':duration seconds'
        """;
    jdbcClient.sql(sql).params("duration", cutoff).update();
  }

  @Transactional
  @Override
  public Page<UserDbo> findAll(Pageable pageable) {
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
