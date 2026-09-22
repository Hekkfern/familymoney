package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dtos.CreateBalanceDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateBalanceDto;
import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.types.BalanceId;
import com.familymoney.domains.transactions.types.GroupId;
import java.util.List;
import java.util.Optional;

/** Repository interface for persisting balances between two members of a group. */
public interface BalanceRepository {

  /**
   * Creates a balance.
   *
   * @param data the balance values to store
   */
  void create(CreateBalanceDto data);

  /**
   * Updates the non-null fields of a balance.
   *
   * @param id the identifier of the balance to update
   * @param dto the values to update
   */
  void updateById(BalanceId id, UpdateBalanceDto dto);

  /**
   * Finds a balance by its identifier.
   *
   * @param id the identifier of the balance to find
   * @return the balance when it exists, or an empty optional otherwise
   */
  Optional<BalanceEntity> findById(BalanceId id);

  /**
   * Finds every balance for a group.
   *
   * @param groupId the group identifier
   * @return the balances recorded for the group
   */
  List<BalanceEntity> findByGroupId(GroupId groupId);
}
