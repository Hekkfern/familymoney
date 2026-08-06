package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dtos.CreateBalanceDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateBalanceDto;
import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.types.BalanceId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import java.util.List;
import java.util.Optional;

/**
 * The IBalanceRepository interface defines the contract for managing balance entries within the
 * family money application. It provides methods for creating, retrieving, and updating balance
 * entries that represent the money owed between users within the context of a specific group.
 */
public interface IBalanceRepository {

  /**
   * Creates a new balance entry for a specific group and two users.
   *
   * <p>A new balance is initialized with a default money value of zero, indicating that there are
   * no outstanding debts between the users at creation time. The entry can then be updated as
   * transactions occur within the group.
   *
   * <p>The {@code user1} and {@code user2} fields are interchangeable: the balance between user1
   * and user2 is the same balance as the one between user2 and user1.
   *
   * @param data values to store
   * @return an Optional containing the created BalanceDbo if the creation was successful, or an
   *     empty Optional if the creation failed (e.g., due to invalid input or database constraints)
   */
  Optional<BalanceEntity> create(CreateBalanceDto data);

  /**
   * Retrieves a list of balance entries associated with a specific group.
   *
   * @param groupId the identifier of the group for which to retrieve balance entries
   * @return a list of BalanceDbo objects representing the balances associated with the specified
   *     group. If no balances are found for the group, an empty list is returned.
   */
  List<BalanceEntity> findByGroup(GroupId groupId);

  /**
   * Retrieves a list of balance entries for a specific user within a specific group.
   *
   * @param userId the identifier of the user for whom to retrieve balance entries
   * @param groupId the identifier of the group for which to retrieve balance entries
   * @return a list of BalanceDbo objects representing the balances associated with the specified
   *     user and group. If no balances are found for the user and group, an empty list is returned.
   */
  List<BalanceEntity> findByUserAndGroup(UserId userId, GroupId groupId);

  /**
   * Updates the balance entry identified by the given BalanceId with the provided data.
   *
   * @param id the identifier of the balance entry to be updated
   * @param data the data containing the updated information for the balance entry. Non-null fields
   *     in the UpdateBalanceDbo will be used to update the corresponding fields in the balance
   *     entry.
   * @return true if the update was successful (i.e., the balance entry was found and updated), or
   *     false if the balance entry was not found or the update failed for any reason (e.g., invalid
   *     input, database constraints).
   */
  boolean updateById(final BalanceId id, UpdateBalanceDto data);

  /**
   * Retrieves a balance entry by its unique identifier.
   *
   * @param id the identifier of the balance entry to be retrieved
   * @return an Optional containing the BalanceDbo if a balance entry with the specified identifier
   *     exists, or an empty Optional if no such balance entry is found
   */
  Optional<BalanceEntity> findById(BalanceId id);
}
