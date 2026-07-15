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

package com.epam.reportportal.base.core.events.attachment;

import static com.epam.reportportal.base.core.configs.rabbit.InternalConfiguration.EXCHANGE_ATTACHMENT;
import static com.epam.reportportal.base.core.configs.rabbit.InternalConfiguration.QUEUE_ATTACHMENT_EXTERNAL_LOAD;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Forwards external attachment load events to RabbitMQ after a successful database transaction
 * commit. When an event is published without an active transaction, it is forwarded immediately.
 */
@Component
public class ExternalAttachmentLoadRabbitPublisher {

  private final AmqpTemplate rabbitTemplate;

  public ExternalAttachmentLoadRabbitPublisher(
      @Qualifier("rabbitTemplate") AmqpTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @Async(value = "eventListenerExecutor")
  @TransactionalEventListener(phase = AFTER_COMMIT, fallbackExecution = true)
  public void onApplicationEvent(ExternalAttachmentLoadEvent event) {
    rabbitTemplate.convertAndSend(EXCHANGE_ATTACHMENT, QUEUE_ATTACHMENT_EXTERNAL_LOAD, event);
  }
}
