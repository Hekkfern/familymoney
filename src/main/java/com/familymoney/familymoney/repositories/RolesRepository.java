package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.types.UserId;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RolesRepository implements IRolesRepository {

  private final JdbcClient jdbcClient;

  @Override
  @NonNull
  public String getRoleByUserId(@NonNull UserId userId) {
    var sql =
        """
        SELECT r.name
        FROM users_roles ur
        JOIN roles r ON ur.role_id = r.id
        WHERE ur.user_id = :userId
        """;
    return jdbcClient.sql(sql).param("userId", userId.value()).query(String.class).single();
  }

  @Override
  public void setRoleForUserId(@NonNull UserId userId, @NonNull String role) {
    var sql =
        """
        WITH r AS (
            SELECT id AS role_id FROM roles WHERE name = :role
        )
        INSERT INTO users_roles (user_id, role_id)
        SELECT :userId, r.role_id FROM r
        ON CONFLICT (user_id) DO UPDATE
            SET role_id = EXCLUDED.role_id;
        """;
    jdbcClient.sql(sql).param("userId", userId.value()).param("role", role).update();
  }
}
