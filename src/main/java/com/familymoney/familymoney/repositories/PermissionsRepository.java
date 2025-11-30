package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.types.UserId;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PermissionsRepository implements IPermissionsRepository {

  private final JdbcClient jdbcClient;

  @Override
  @NonNull
  public List<String> getPermissionsByUserId(@NonNull UserId userId) {
    var sql =
        """
        SELECT p.name
        FROM users_permissions up
        JOIN permissions p ON up.permission_id = p.id
        WHERE up.user_id = :userId
        """;
    return jdbcClient
        .sql(sql)
        .param("userId", userId.value())
        .query(String.class)
        .list()
        .stream()
        .filter(java.util.Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public void setPermissionsForUserId(@NonNull UserId userId, @NonNull List<String> permissions) {
    assert (!permissions.isEmpty());
    var sql =
        """
        INSERT INTO users_permissions (user_id, permission_id)
        SELECT :userId, p.id
        FROM permissions p
        WHERE p.name IN (:names)
          AND NOT EXISTS (
            SELECT 1 FROM users_permissions up
            WHERE up.user_id = :userId AND up.permission_id = p.id
          )
        """;
    jdbcClient.sql(sql).param("userId", userId.value()).param("names", permissions).update();
  }

  @Override
  public void deletePermissionsByUserId(@NonNull UserId userId, @NonNull List<String> permissions) {
    assert (!permissions.isEmpty());
    var sql =
        """
        DELETE FROM users_permissions
        WHERE user_id = :userId
          AND permission_id IN (
            SELECT id FROM permissions WHERE name IN (:names)
          )
        """;
    jdbcClient.sql(sql).param("userId", userId.value()).param("names", permissions).update();
  }

  @Override
  public void setRoleForUserId(@NonNull UserId userId, @NonNull String role) {
    var sql =
        """
        INSERT INTO users_permissions (user_id, permission_id)
        SELECT :userId, rp.permission_id
        FROM roles r
        JOIN roles_permissions rp ON r.id = rp.role_id
        WHERE r.name = :role
          AND NOT EXISTS (
            SELECT 1 FROM users_permissions up
            WHERE up.user_id = :userId AND up.permission_id = rp.permission_id
          )
        """;
    jdbcClient.sql(sql).param("userId", userId.value()).param("role", role).update();
  }
}
