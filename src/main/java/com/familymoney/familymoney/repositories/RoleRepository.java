package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.types.Role;
import com.familymoney.familymoney.types.UserId;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoleRepository implements IRoleRepository {

  private final JdbcClient jdbcClient;

  @Override
  public Role getRoleByUserId(UserId userId) {
    var sql =
        """
        SELECT r.name
        FROM users_roles ur
        JOIN roles r ON ur.role_id = r.id
        WHERE ur.user_id = :userId
        """;
    val role = jdbcClient.sql(sql).param("userId", userId.value()).query(String.class).single();
    return Role.fromString(role);
  }

  @Override
  public void setRoleForUserId(UserId userId, Role role) {
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
    jdbcClient.sql(sql).param("userId", userId.value()).param("role", role.toString()).update();
  }
}
