package com.familymoney.domains.user.repositories;

import com.familymoney.domains.user.types.Role;
import com.familymoney.domains.user.types.UserId;
import java.util.Optional;

/** Repository contract for user role persistence and lookup. */
public interface IRoleRepository {

  /**
   * Retrieve the role assigned to the given user id.
   *
   * @param userId the id of the user whose role should be returned. Must not be null.
   * @return an {@link Optional} containing the {@link Role} if the user has an assigned role;
   *     otherwise an empty Optional when no mapping exists.
   */
  Optional<Role> getRoleByUserId(UserId userId);

  /**
   * Assign or update the role for a given user id.
   *
   * <p>Typical implementations will insert a new mapping or update an existing one. The method
   * should be safe to call repeatedly (idempotent for the same role).
   *
   * @param userId the id of the user to assign the role to. Must not be null.
   * @param role the role to assign. Must not be null.
   * @return {@code true} when the role was inserted or updated successfully; {@code false} when no
   *     change occurred because of an error.
   */
  boolean setRoleForUserId(UserId userId, Role role);
}
