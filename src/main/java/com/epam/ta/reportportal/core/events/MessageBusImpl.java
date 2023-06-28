/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.events;

import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.EXCHANGE_ACTIVITY;
import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.EXCHANGE_ATTACHMENT;
import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.EXCHANGE_EVENTS;
import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.QUEUE_ATTACHMENT_DELETE;

import com.epam.ta.reportportal.core.events.attachment.DeleteAttachmentEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import org.springframework.amqp.core.AmqpTemplate;

public class MessageBusImpl implements MessageBus {

  private final AmqpTemplate amqpTemplate;

  public MessageBusImpl(AmqpTemplate amqpTemplate) {
    this.amqpTemplate = amqpTemplate;
  }

  @Override
  public void publish(String exchange, String route, Object o) {
    this.amqpTemplate.convertAndSend(exchange, route, o);
  }

  @Override
  public void publish(String route, Object o) {
    this.amqpTemplate.convertSendAndReceive(route, o);
  }

  @Override
  public void broadcastEvent(Object o) {
    this.amqpTemplate.convertAndSend(EXCHANGE_EVENTS, "", o);
  }

  /**
   * Publishes activity to the queue with the following routing key
   * <pre>{@code activity.<project-id>.<entity-type>.<action>}</pre>
   *
   * @param event Activity event to be converted to Activity object
   */
  @Override
  public void publishActivity(ActivityEvent event) {
    final Activity activity = event.toActivity();
    if (activity != null) {
      String key =
          "activity." + activity.getProjectId() + "." + activity.getObjectType() + "."
              + activity.getEventName();
      this.amqpTemplate.convertAndSend(EXCHANGE_ACTIVITY, key, activity);
    }
  }

  @Override
  public void publishDeleteAttachmentEvent(DeleteAttachmentEvent event) {

    amqpTemplate.convertAndSend(EXCHANGE_ATTACHMENT, QUEUE_ATTACHMENT_DELETE, event);

  }
}
