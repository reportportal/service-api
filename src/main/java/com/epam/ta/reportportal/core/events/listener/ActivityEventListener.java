/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.epam.ta.reportportal.core.events.listener;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.core.events.MessageBus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener for catching activities after transaction commit and sending to RabbitMQ.
 *
 * @author Ryhor_Kukharenka
 */
@Component
public class ActivityEventListener {

  private final MessageBus messageBus;

  public ActivityEventListener(MessageBus messageBus) {
    this.messageBus = messageBus;
  }

  @Async(value = "eventListenerExecutor")
  @TransactionalEventListener
  public void onApplicationEvent(ActivityEvent event) {
    messageBus.publishActivity(event);
  }

}
