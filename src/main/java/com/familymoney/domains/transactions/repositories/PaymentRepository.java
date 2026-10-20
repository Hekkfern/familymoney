package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.PaymentNotFoundException;
import com.familymoney.domains.transactions.repositories.dbos.CreatePaymentDbo;
import com.familymoney.domains.transactions.repositories.dbos.UpdatePaymentDbo;
import com.familymoney.domains.transactions.repositories.entitites.PaymentEntity;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository interface for persisting completed payments between members of a group.
 *
 * <p>A payment records a single amount transferred from its {@code to} to its {@code from},
 * settling part or all of the debt between them. Implementations create, update, delete, and
 * retrieve payments, including paged queries for a group.
 */
public interface PaymentRepository {

  /**
   * Creates a payment.
   *
   * @param dto the payment values to store
   * @throws GroupNotFoundException if the group of the payment does not exist
   * @throws UserNotFoundException if the from, the to, or the creator does not exist
   */
  void create(CreatePaymentDbo dto);

  /**
   * Updates the non-null fields of a payment.
   *
   * @param id the identifier of the payment to update
   * @param dto the values to update
   * @throws PaymentNotFoundException if no payment with the given ID exists
   * @throws UserNotFoundException if the updated from or to does not exist
   */
  void updateById(PaymentId id, UpdatePaymentDbo dto);

  /**
   * Deletes a payment.
   *
   * @param id the identifier of the payment to delete
   */
  void deleteById(PaymentId id);

  /**
   * Finds a payment by its identifier.
   *
   * @param id the identifier of the payment to find
   * @return the payment when it exists, or an empty optional otherwise
   */
  Optional<PaymentEntity> findById(PaymentId id);

  /**
   * Finds the requested payments in a group.
   *
   * @param groupId the group identifier
   * @param ids the payment identifiers to retrieve
   * @return the matching payments, with no ordering guarantee
   */
  List<PaymentEntity> findAllByGroupIdAndIds(final GroupId groupId, final Set<PaymentId> ids);

  /**
   * Finds a page of payments for a group, ordered from newest to oldest completion time.
   *
   * @param groupId the group identifier
   * @return a page of payments for the group
   */
  Page<PaymentEntity> findAllByGroupId(GroupId groupId, Pageable pageable);

  /**
   * Deletes a batch of payments that belong to groups soft-deleted before the given instant.
   *
   * @param deletedBefore only payments of groups soft-deleted before this instant are deleted
   * @param batchSize the maximum number of payments to delete
   * @return the number of deleted payments
   */
  int deletePaymentsOfPurgeableGroups(Instant deletedBefore, int batchSize);
}
