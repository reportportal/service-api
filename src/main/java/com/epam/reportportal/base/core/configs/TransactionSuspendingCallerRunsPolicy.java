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

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs a rejected task in the submitting thread, like {@link ThreadPoolExecutor.CallerRunsPolicy}, but with the
 * submitter's transaction suspended. Tasks are often submitted from a transaction's {@code afterCommit} callback
 * ({@code @Async} {@code @TransactionalEventListener}s): run in place there, every {@code @Transactional} method of the
 * task would join the already committed transaction and its writes would never be committed. Discards the task when the
 * executor is shut down.
 */
public class TransactionSuspendingCallerRunsPolicy implements RejectedExecutionHandler {

  private final TransactionTemplate withoutTransaction;

  public TransactionSuspendingCallerRunsPolicy(PlatformTransactionManager transactionManager) {
    this.withoutTransaction = new TransactionTemplate(transactionManager);
    this.withoutTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
  }

  @Override
  public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
    if (executor.isShutdown()) {
      return;
    }
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      withoutTransaction.executeWithoutResult(status -> task.run());
    } else {
      task.run();
    }
  }
}
