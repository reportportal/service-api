/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.util.email.strategy;

import com.epam.reportportal.base.util.email.MailServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Realisation Strategy pattern for chosen right email template.
 *
 * @author Andrei Piankouski
 */
public abstract class AbstractEmailNotificationStrategy implements EmailNotificationStrategy {

  protected static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractEmailNotificationStrategy.class);

  protected final MailServiceFactory mailServiceFactory;
  protected final ThreadPoolTaskExecutor emailExecutorService;

  /**
   * Creates a strategy with mail and executor dependencies.
   *
   * @param mailServiceFactory   mail service factory
   * @param emailExecutorService thread pool for async email dispatch
   */
  @Autowired
  public AbstractEmailNotificationStrategy(MailServiceFactory mailServiceFactory,
      ThreadPoolTaskExecutor emailExecutorService) {
    this.mailServiceFactory = mailServiceFactory;
    this.emailExecutorService = emailExecutorService;
  }
}
