package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dtos.BalanceKey;
import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.repositories.exceptions.CreateBalanceException;
import com.familymoney.domains.transactions.repositories.exceptions.UpdateBalanceException;
import com.familymoney.domains.transactions.types.GroupId;
import java.util.List;
import java.util.Optional;
import org.javamoney.moneta.Money;

/**
 * Repository interface for persisting balances between two members of a group.
 *
 * <p>A balance tracks the net signed amount owed between an unordered pair of users within a
 * group: a single row exists per pair, and the sign of the stored amount, not the order in which
 * the two users are supplied, determines which of them currently owes the other. Callers do not
 * need to know or preserve a particular ordering of the two users when creating, updating, or
 * looking up a balance through a {@link BalanceKey}.
 */
public interface BalanceRepository {

  /**
   * Creates a balance row, initialized to zero, for the unordered pair of users in {@code key}.
   *
   * @param key the group and pair of users the balance belongs to
   * @throws CreateBalanceException if the balance could not be created
   */
  void create(BalanceKey key);

  /**
   * Adds {@code delta} to the balance identified by {@code key}. A positive amount shifts the
   * balance so that {@code key.user1()} is owed more by {@code key.user2()}; a negative amount
   * shifts it in the opposite direction, and can flip which of the two users is currently in
   * debt.
   *
   * @param key the group and pair of users the balance belongs to
   * @param delta the signed amount to add to the current balance
   * @throws UpdateBalanceException if no group with the given ID exists, if {@code delta}'s
   *     currency does not match the group's currency, or if no balance exists for the given key
   */
  void incrementByKey(BalanceKey key, Money delta);

  /**
   * Finds the balance between the unordered pair of users in {@code key}.
   *
   * @param key the group and pair of users the balance belongs to
   * @return an {@link Optional} containing the {@link BalanceEntity} if found, otherwise empty
   */
  Optional<BalanceEntity> findByKey(BalanceKey key);

  /**
   * Finds all balances recorded for a group.
   *
   * @param groupId the group identifier
   * @return a list of {@link BalanceEntity} values for the group; empty if the group has no
   *     recorded balances
   */
  List<BalanceEntity> findByGroupId(GroupId groupId);
}
