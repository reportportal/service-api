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

package com.epam.reportportal.base.core.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.epam.reportportal.base.infrastructure.persistence.dao.StatisticsFieldRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * A task rejected by a saturated executor while it is submitted from a transaction's {@code afterCommit} callback must
 * still commit its own writes. A change to a managed entity shows the difference: joined to the committed transaction
 * it is never flushed.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TransactionSuspendingCallerRunsPolicyTest extends BaseMvcTest {

  private static final String PROBE = "caller_runs_policy_probe";
  private static final String PROBE_HANDLED = PROBE + "$handled";

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private StatisticsFieldRepository statisticsFieldRepository;

  @Autowired
  @Qualifier("domainEventExecutor")
  private TaskExecutor domainEventExecutor;

  @BeforeEach
  void createProbe() {
    jdbcTemplate.update("INSERT INTO statistics_field (name) VALUES (?)", PROBE);
  }

  @AfterEach
  void deleteProbe() {
    jdbcTemplate.update("DELETE FROM statistics_field WHERE name IN (?, ?)", PROBE, PROBE_HANDLED);
  }

  @Test
  void domainEventExecutorUsesThePolicy() {
    assertInstanceOf(TransactionSuspendingCallerRunsPolicy.class,
        ((ThreadPoolTaskExecutor) domainEventExecutor).getThreadPoolExecutor()
            .getRejectedExecutionHandler());
  }

  @Test
  void rejectedTaskSubmittedAfterCommitCommitsItsWrites() throws InterruptedException {
    assertRejectedTaskCommitsItsWrites(submit -> new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        submit.run();
      }
    });
  }

  /**
   * An AFTER_COMMIT {@code @TransactionalEventListener} is invoked from {@code afterCompletion}, where synchronization
   * is no longer active but the committed transaction is still bound to the thread.
   */
  @Test
  void rejectedTaskSubmittedAfterCompletionCommitsItsWrites() throws InterruptedException {
    assertRejectedTaskCommitsItsWrites(submit -> new TransactionSynchronization() {
      @Override
      public void afterCompletion(int status) {
        submit.run();
      }
    });
  }

  private void assertRejectedTaskCommitsItsWrites(Function<Runnable, TransactionSynchronization> callback)
      throws InterruptedException {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(1), new TransactionSuspendingCallerRunsPolicy(transactionManager));
    CountDownLatch release = new CountDownLatch(1);
    AtomicReference<Thread> handlerThread = new AtomicReference<>();
    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    try {
      // saturate: one running task and one queued task
      executor.execute(() -> awaitQuietly(release));
      executor.execute(() -> {
      });

      // what an @Async @TransactionalEventListener whose handler is @Transactional (REQUIRED) does
      Runnable submit = () -> executor.execute(() -> transactionTemplate.executeWithoutResult(inner -> {
        handlerThread.set(Thread.currentThread());
        statisticsFieldRepository.findByName(PROBE).ifPresent(field -> field.setName(PROBE_HANDLED));
      }));
      transactionTemplate.executeWithoutResult(
          status -> TransactionSynchronizationManager.registerSynchronization(callback.apply(submit)));
    } finally {
      release.countDown();
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    assertEquals(Thread.currentThread(), handlerThread.get(), "the rejected task runs in the submitting thread");
    assertEquals(1, countFields(PROBE_HANDLED));
    assertEquals(0, countFields(PROBE));
  }

  private int countFields(String name) {
    return jdbcTemplate.queryForObject("SELECT count(*) FROM statistics_field WHERE name = ?", Integer.class,
        name);
  }

  private static void awaitQuietly(CountDownLatch latch) {
    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
