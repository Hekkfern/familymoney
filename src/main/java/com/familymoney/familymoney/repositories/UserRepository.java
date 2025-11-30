package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.repositories.rowmappers.UserDboRowMapper;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.Username;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepository implements IUserRepository {

    private final JdbcClient jdbcClient;

    @Override
    @NonNull
    public Optional<UserDbo> create(@NonNull Username username, @NonNull Email email, @NonNull String passwordHash) {
        var sql = """
                INSERT INTO users (username, email, hashed_password)
                VALUES (:username, :email, :passwordHash)
                RETURNING id, username, email, hashed_password, email_verified, created_at, updated_at, is_enabled
                """;
        return jdbcClient.sql(sql)
                .param("username", username.toString())
                .param("email", email.toString())
                .param("passwordHash", passwordHash)
                .query(new UserDboRowMapper())
                .optional();
    }

    @Override
    @NonNull
    public Optional<UserDbo> findById(@NonNull UserId id) {
        var sql = """
                SELECT id, username, email, hashed_password, created_at, updated_at, email_verified, is_enabled
                FROM users
                WHERE id = :id
                """;
        return jdbcClient
                .sql(sql)
                .param("id", id.toString())
                .query(new UserDboRowMapper())
                .optional();
    }

    @Override
    @NonNull
    public Optional<UserDbo> findByEmail(@NonNull Email email) {
        var sql = """
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
    @NonNull
    public Optional<UserDbo> findByUsername(@NonNull Username username) {
        var sql = """
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
    public boolean existsByEmailOrUsername(@NonNull Email email, @NonNull Username username) {
        var sql = """
                SELECT COUNT(*)
                FROM users
                WHERE email = :email OR username = :username
                """;
        var count = jdbcClient
                .sql(sql)
                .param("email", email.toString())
                .param("username", username.toString())
                .query(Integer.class)
                .single();
        return count > 0;
    }

    @Override
    public void update(@NonNull UserId id, @NonNull Optional<Username> username, @NonNull Optional<Email> email) {
        var sql = """
                UPDATE users
                SET username = COALESCE(:username, username),
                    email = COALESCE(:email, email)
                WHERE id = :id
                """;
        jdbcClient.sql(sql)
                .param("id", id.toString())
                .param("username", username.isPresent() ? username.toString() : null)
                .param("email", email.isPresent() ? email.toString() : null)
                .update();
    }

    @Override
    public void updatePassword(@NonNull UserId id, @NonNull String newPasswordHash) {
        var sql = """
                UPDATE users
                SET hashed_password = :passwordHash
                WHERE id = :id
                """;
        jdbcClient.sql(sql)
                .param("id", id.value())
                .param("passwordHash", newPasswordHash)
                .update();
    }

    @Override
    public void deleteById(@NonNull UserId id) {
        var sql = """
                DELETE FROM users
                WHERE id = :id
                """;
        jdbcClient.sql(sql).param("id", id.value()).update();
    }

    @Override
    public void deleteByIsUnverifiedAndOlderThan(@NonNull Duration cutoff) {
        var sql = """
                DELETE FROM users
                WHERE email_verified = FALSE
                  AND created_at < NOW() - INTERVAL ':duration seconds'
                """;
        jdbcClient.sql(sql).params("duration", cutoff).update();
    }
}
