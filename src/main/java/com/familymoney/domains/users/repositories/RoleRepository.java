package com.familymoney.domains.users.repositories;

import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoleRepository implements IRoleRepository {

  private final DSLContext db;

  @Override
  public Optional<Role> getRoleByUserId(final UserId userId) {
    return db.select(DSL.field("r.name", String.class))
        .from(DSL.table("users_roles").as("ur"))
        .join(DSL.table("roles").as("r"))
        .on(DSL.field("ur.role_id").eq(DSL.field("r.id")))
        .where(DSL.field("ur.user_id").eq(userId.value()))
        .fetchOptional()
        .map(r -> r.get(0, String.class))
        .map(Role::fromString);
  }

  @Override
  public boolean setRoleForUserId(final UserId userId, final Role role) {
    final String sql =
        """
        WITH r AS (
            SELECT id AS role_id FROM roles WHERE name = ?
        )
        INSERT INTO users_roles (user_id, role_id)
        SELECT ?, r.role_id FROM r
        ON CONFLICT (user_id) DO UPDATE
            SET role_id = EXCLUDED.role_id;
        """;
    final int rowsAffected = db.query(sql, role.toString(), userId.value()).execute();
    return rowsAffected > 0;
  }
}
