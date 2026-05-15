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

package com.epam.reportportal.base.core.events.attachment;

import static com.epam.reportportal.base.core.configs.rabbit.InternalConfiguration.EXCHANGE_ATTACHMENT;
import static com.epam.reportportal.base.core.configs.rabbit.InternalConfiguration.QUEUE_ATTACHMENT_EXTERNAL_LOAD;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Publishes {@link ExternalAttachmentLoadEvent} messages to the {@code attachment} exchange with
 * routing key
 * {@link
 * com.epam.reportportal.base.core.configs.rabbit.InternalConfiguration#QUEUE_ATTACHMENT_EXTERNAL_LOAD}.
 * The downstream consumer is expected to download the external binary and attach it to the log row
 * referenced by {@code logId}.
 */
@Service
public class ExternalAttachmentLoadProducer {

  private final AmqpTemplate amqpTemplate;

  public ExternalAttachmentLoadProducer(
      @Qualifier("rabbitTemplate") AmqpTemplate amqpTemplate) {
    this.amqpTemplate = amqpTemplate;
  }

  public void publish(Long logId, Long projectId, Long launchId, Long testItemId,
      String attachmentExternalId) {
    amqpTemplate.convertAndSend(EXCHANGE_ATTACHMENT, QUEUE_ATTACHMENT_EXTERNAL_LOAD,
        new ExternalAttachmentLoadEvent(logId, projectId, launchId, testItemId,
            attachmentExternalId));
  }
}
