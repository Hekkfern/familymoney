package com.familymoney.utils;

import org.jooq.Key;
import org.springframework.dao.DataAccessException;

/**
 * Utility methods for identifying which database constraint caused a data access failure.
 *
 * <p>Constraints are referenced through the jOOQ-generated {@code Keys} class so that renaming or
 * removing a constraint in the database schema results in a compilation error instead of a silent
 * mismatch at runtime.
 */
public final class ConstraintViolationUtils {

  private ConstraintViolationUtils() {
    /* This utility class should not be instantiated */
  }

  /**
   * Checks whether the given exception was caused by a violation of the given constraint.
   *
   * @param exception the data access exception raised by the database operation
   * @param constraint the jOOQ-generated key describing the constraint to check
   * @return {@code true} if the root cause of the exception references the constraint; otherwise
   *     {@code false}
   */
  public static boolean isConstraintViolated(
      final DataAccessException exception, final Key<?> constraint) {
    final String message = exception.getMostSpecificCause().getMessage();
    return message != null && message.contains(constraint.getName());
  }
}
