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

package com.epam.reportportal.base.ws.rabbit;

import static com.epam.reportportal.base.core.configs.rabbit.InternalConfiguration.QUEUE_ATTACHMENT_EXTERNAL_LOAD;

import com.epam.reportportal.base.core.events.attachment.ExternalAttachmentLoadEvent;
import com.epam.reportportal.base.core.events.attachment.ExternalAttachmentLoadService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer that downloads external attachments and links them to existing logs.
 */
@Component
@RequiredArgsConstructor
public class ExternalAttachmentLoadConsumer {

  private final ExternalAttachmentLoadService externalAttachmentLoadService;

  @RabbitListener(queues = QUEUE_ATTACHMENT_EXTERNAL_LOAD,
      containerFactory = "rabbitListenerContainerFactory")
  public void onEvent(@Payload ExternalAttachmentLoadEvent event) {
    externalAttachmentLoadService.loadAttachment(event);
  }
}
