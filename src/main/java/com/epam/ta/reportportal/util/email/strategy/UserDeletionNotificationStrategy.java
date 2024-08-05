/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.util.email.strategy;

import com.epam.ta.reportportal.util.email.MailServiceFactory;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 * @author Andrei Piankouski
 */
@Service
public class UserDeletionNotificationStrategy extends AbstractEmailNotificationStrategy {

  @Autowired
  public UserDeletionNotificationStrategy(MailServiceFactory mailServiceFactory,
      ThreadPoolTaskExecutor emailExecutorService) {
    super(mailServiceFactory, emailExecutorService);
  }

  @Override
  public void sendEmail(String recipient, Map<String, Object> params) {
    try {
      emailExecutorService.execute(() -> mailServiceFactory.getDefaultEmailService(false)
          .sendAccountDeletionByRetentionNotification(recipient));
    } catch (Exception e) {
      LOGGER.warn("Unable to send email.", e);
    }
  }
}
