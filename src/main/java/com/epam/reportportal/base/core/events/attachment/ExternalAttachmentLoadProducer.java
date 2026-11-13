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

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Publishes {@link ExternalAttachmentLoadEvent} application events. The events are forwarded to
 * RabbitMQ by {@link ExternalAttachmentLoadRabbitPublisher} after the current transaction commits.
 */
@Service
@RequiredArgsConstructor
public class ExternalAttachmentLoadProducer {

  private final ApplicationEventPublisher applicationEventPublisher;

  public void publish(String pluginId, String pluginCommandName, Long logId, Long projectId,
      Long launchId, Long testItemId, String attachmentExternalId) {
    publish(pluginId, pluginCommandName, logId, projectId, launchId, testItemId,
        attachmentExternalId, null);
  }

  public void publish(String pluginId, String pluginCommandName, Long logId, Long projectId,
      Long launchId, Long testItemId, String attachmentExternalId, String attachmentAttributeKey) {
    applicationEventPublisher.publishEvent(
        new ExternalAttachmentLoadEvent(pluginId, pluginCommandName, logId, projectId, launchId,
            testItemId, attachmentExternalId, attachmentAttributeKey));
  }
}
