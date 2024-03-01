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

package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.core.configs.Conditions;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownSignalException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.ListenerContainerConsumerFailedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * Finds a queue that doesn't have any connected consumers and set's it to consumer that should be
 * restarted, so it can be registered with a different queue.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
@Conditional(Conditions.NotTestCondition.class)
public class ConsumerEventListener implements
    ApplicationListener<ListenerContainerConsumerFailedEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerEventListener.class);

  @Autowired
  @Qualifier("queues")
  private List<Queue> queues;

  @Autowired
  private ConnectionFactory connectionFactory;

  @Override
  public void onApplicationEvent(ListenerContainerConsumerFailedEvent event) {
    Object source = event.getSource();
    if (source instanceof AbstractMessageListenerContainer) {
      AbstractMessageListenerContainer listenerContainer = (AbstractMessageListenerContainer) source;
      Throwable throwable = event.getThrowable();
      if (throwable.getCause() instanceof IOException && throwable.getCause()
          .getCause() instanceof ShutdownSignalException
          && throwable.getCause().getCause().getMessage().contains("in exclusive use")) {
        for (Queue q : queues) {
          if (getQueueConsumerCount(q) == 0) {
            listenerContainer.setQueues(q);
            LOGGER.info("Restarting consumer with a queue {}", q.getName());
          }
        }
      }
    }
  }

  private int getQueueConsumerCount(Queue queue) {
    try (Channel channel = connectionFactory.createConnection().createChannel(false)) {
      return channel.queueDeclarePassive(queue.getName()).getConsumerCount();
    } catch (IOException | TimeoutException e) {
      throw new ReportPortalException(e.getMessage());
    }
  }
}
