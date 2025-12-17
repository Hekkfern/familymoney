package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.types.Role;
import com.familymoney.familymoney.types.UserId;
import java.util.Optional;

public interface IRoleRepository {

  /**
   * Get the role of the selected User ID
   *
   * @param userId ID of the user
   * @return Role wrapped in Optional, or empty Optional if not found
   */
  Optional<Role> getRoleByUserId(UserId userId);

  /**
   * Set the role for the selected User ID
   *
   * @param userId ID of the user
   * @param role Role to be assigned
   * @return true if the operation was successful, false otherwise
   */
  boolean setRoleForUserId(UserId userId, Role role);
}
