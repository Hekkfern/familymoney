package com.familymoney.domains.transactions.services.data;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import org.javamoney.moneta.Money;

/**
 * Represents payment data returned by the service.
 *
 * @param id the payment identifier
 * @param groupId the group identifier
 * @param description the payment description
 * @param money the paid monetary amount
 * @param doneAt the payment completion time
 * @param from the user who made the payment
 * @param to the user who received the payment
 */
public record PaymentData(
    PaymentId id,
    GroupId groupId,
    Description description,
    Money money,
    Instant doneAt,
    UserId from,
    UserId to) {}
