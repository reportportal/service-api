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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discards rejected tasks like {@link ThreadPoolExecutor.DiscardPolicy}, but counts them and logs a WARN at most once
 * per {@link #LOG_INTERVAL_NANOS} so a saturated executor is visible without flooding the log.
 */
public class LoggingDiscardPolicy implements RejectedExecutionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingDiscardPolicy.class);

  static final long LOG_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(10);

  private final String executorName;
  private final AtomicLong discarded = new AtomicLong();
  private final AtomicLong lastLogNanos = new AtomicLong(System.nanoTime() - LOG_INTERVAL_NANOS);

  public LoggingDiscardPolicy(String executorName) {
    this.executorName = executorName;
  }

  @Override
  public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
    long total = discarded.incrementAndGet();
    long now = System.nanoTime();
    long last = lastLogNanos.get();
    if (now - last >= LOG_INTERVAL_NANOS && lastLogNanos.compareAndSet(last, now)) {
      LOGGER.warn(
          "Executor '{}' is saturated (active threads: {}, queue size: {}): task discarded, {} discarded in total",
          executorName, executor.getActiveCount(), executor.getQueue().size(), total
      );
    }
  }

  public long getDiscardedCount() {
    return discarded.get();
  }
}
