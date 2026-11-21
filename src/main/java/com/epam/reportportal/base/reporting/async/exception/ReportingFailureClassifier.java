/*
 * Copyright 2026 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.reporting.async.exception;

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.reporting.async.config.RequestType;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.SQLTransientException;
import java.util.Set;
import java.util.function.Predicate;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.AmqpTimeoutException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.CannotCreateTransactionException;

/**
 * Decides from the cause chain of a reporting failure whether the message may be processed again.
 *
 * <p>Retryable: the NOT_FOUND errors of messages that arrived before their parent, and transient infrastructure
 * failures (lock conflicts, deadlocks, timeouts, lost or refused connections). A unique or foreign key violation is
 * never retryable. A broker failure is not retryable for a LOG message, because the log row may already be committed.
 */
public final class ReportingFailureClassifier {

  private static final int MAX_CAUSE_DEPTH = 10;

  private static final Set<ErrorType> RETRYABLE_ERROR_TYPES = Set.of(
      ErrorType.LAUNCH_NOT_FOUND,
      ErrorType.TEST_SUITE_NOT_FOUND,
      ErrorType.TEST_ITEM_NOT_FOUND);

  /**
   * serialization_failure, deadlock_detected, lock_not_available.
   */
  private static final Set<String> LOCK_CONFLICT_SQL_STATES = Set.of("40001", "40P01", "55P03");

  /**
   * Lock conflicts plus query_canceled (statement timeout), admin_shutdown, crash_shutdown, cannot_connect_now,
   * too_many_connections. Class 08 (connection exception) is matched by prefix.
   */
  private static final Set<String> TRANSIENT_SQL_STATES = Set.of("40001", "40P01", "55P03", "57014",
      "57P01", "57P02", "57P03", "53300");

  private ReportingFailureClassifier() {
    //static only
  }

  /**
   * @param failure     the failure thrown by the message handler
   * @param requestType request type of the failed message, may be {@code null}
   * @return whether the message may be sent to the retry queues
   */
  public static boolean isRetryable(Throwable failure, RequestType requestType) {
    if (anyCause(failure, DataIntegrityViolationException.class::isInstance)) {
      return false;
    }
    return anyCause(failure, ReportingFailureClassifier::isRetryableNotFound)
        || anyCause(failure, ReportingFailureClassifier::isTransientDatabaseFailure)
        || (requestType != RequestType.LOG && anyCause(failure,
        ReportingFailureClassifier::isTransientBrokerFailure));
  }

  /**
   * @param failure the failure thrown by the message handler
   * @return whether the failure is a lock conflict (deadlock, serialization failure, lock wait timeout) that is likely
   * to succeed on an immediate retry
   */
  public static boolean isLockConflict(Throwable failure) {
    if (anyCause(failure, DataIntegrityViolationException.class::isInstance)) {
      return false;
    }
    return anyCause(failure, cause -> cause instanceof PessimisticLockingFailureException
        || cause instanceof org.hibernate.exception.LockAcquisitionException
        || cause instanceof org.hibernate.PessimisticLockException
        || cause instanceof jakarta.persistence.PessimisticLockException
        || cause instanceof jakarta.persistence.LockTimeoutException
        || (cause instanceof SQLException sqlException
        && LOCK_CONFLICT_SQL_STATES.contains(sqlException.getSQLState())));
  }

  private static boolean isRetryableNotFound(Throwable cause) {
    return cause instanceof ReportPortalException reportPortalException
        && RETRYABLE_ERROR_TYPES.contains(reportPortalException.getErrorType());
  }

  private static boolean isTransientDatabaseFailure(Throwable cause) {
    if (cause instanceof SQLException sqlException && isTransientSqlState(sqlException.getSQLState())) {
      return true;
    }
    return cause instanceof TransientDataAccessException
        || cause instanceof RecoverableDataAccessException
        || cause instanceof DataAccessResourceFailureException
        || cause instanceof CannotCreateTransactionException
        || cause instanceof org.hibernate.exception.JDBCConnectionException
        || cause instanceof org.hibernate.exception.LockAcquisitionException
        || cause instanceof org.hibernate.PessimisticLockException
        || cause instanceof jakarta.persistence.PessimisticLockException
        || cause instanceof jakarta.persistence.LockTimeoutException
        || cause instanceof jakarta.persistence.QueryTimeoutException
        || cause instanceof SQLTransientException
        || cause instanceof SQLRecoverableException;
  }

  private static boolean isTransientSqlState(String sqlState) {
    return sqlState != null && (sqlState.startsWith("08") || TRANSIENT_SQL_STATES.contains(sqlState));
  }

  private static boolean isTransientBrokerFailure(Throwable cause) {
    return cause instanceof AmqpConnectException
        || cause instanceof AmqpIOException
        || cause instanceof AmqpTimeoutException;
  }

  private static boolean anyCause(Throwable failure, Predicate<Throwable> predicate) {
    Throwable current = failure;
    for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {
      if (predicate.test(current)) {
        return true;
      }
      if (current instanceof SQLException sqlException && sqlException.getNextException() != null
          && predicate.test(sqlException.getNextException())) {
        return true;
      }
      Throwable cause = current.getCause();
      current = cause == current ? null : cause;
    }
    return false;
  }
}
