package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dtos.CreatePaymentDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdatePaymentDto;
import com.familymoney.domains.transactions.repositories.entitites.PaymentEntity;
import com.familymoney.domains.transactions.repositories.exceptions.CreatePaymentException;
import com.familymoney.domains.transactions.repositories.exceptions.DeletePaymentException;
import com.familymoney.domains.transactions.repositories.exceptions.UpdatePaymentException;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import java.util.Optional;
import org.springframework.data.domain.Page;

/**
 * Repository interface for persisting completed payments between members of a group.
 *
 * <p>A payment records a single amount transferred from its {@code debitor} to its {@code
 * creditor}, settling part or all of the debt between them. Implementations create, update,
 * delete, and retrieve payments, including paged queries for a group.
 */
public interface PaymentRepository {

  /**
   * Creates a payment.
   *
   * @param dto the payment values to store
   * @throws CreatePaymentException if the payment could not be created, or if its currency does
   *     not match the currency of its group
   */
  void create(CreatePaymentDto dto);

  /**
   * Updates the non-null fields of a payment.
   *
   * @param id the identifier of the payment to update
   * @param dto the values to update
   * @throws UpdatePaymentException if no payment with the given ID exists, or if the updated
   *     amount uses a currency other than the payment's own
   */
  void updateById(PaymentId id, UpdatePaymentDto dto);

  /**
   * Deletes a payment.
   *
   * @param id the identifier of the payment to delete
   * @throws DeletePaymentException if no payment with the given ID exists
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
   * Finds a page of payments for a group, ordered from newest to oldest completion time.
   *
   * @param groupId the group identifier
   * @param page the zero-based page number
   * @param size the maximum number of payments in the page
   * @return a page of payments for the group
   */
  Page<PaymentEntity> findAllByGroupId(GroupId groupId, int page, int size);
}
