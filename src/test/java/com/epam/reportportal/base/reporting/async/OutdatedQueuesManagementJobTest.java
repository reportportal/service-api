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

package com.epam.reportportal.base.reporting.async;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.QueueInfo;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;

class OutdatedQueuesManagementJobTest {

  private final Client managementClient = mock(Client.class);
  private final ReportingShovelService shovelService = mock(ReportingShovelService.class);

  @Test
  void declaresOnlyOneShovelWhileRepublishingIsActive() {
    QueueInfo outdatedQueue = queueInfo("q.reporting.outdated.0", 1);
    when(managementClient.getQueues("/")).thenReturn(List.of(outdatedQueue));
    OutdatedQueuesManagementJob job = job();

    job.run();
    job.run();

    verify(shovelService).republishToReportingExchange(outdatedQueue.getName());
  }

  @Test
  void removesAnEmptyOutdatedQueue() {
    QueueInfo outdatedQueue = queueInfo("q.reporting.outdated.0", 0);
    when(managementClient.getQueues("/")).thenReturn(List.of(outdatedQueue));

    job().run();

    verify(shovelService).removeShovel(outdatedQueue.getName());
    verify(managementClient).deleteQueue("/", outdatedQueue.getName());
  }

  private OutdatedQueuesManagementJob job() {
    return new OutdatedQueuesManagementJob(managementClient, shovelService,
        List.of(new Queue("q.reporting.current.0")), "/", Duration.ZERO);
  }

  private static QueueInfo queueInfo(String name, long messagesReady) {
    QueueInfo queue = new QueueInfo();
    queue.setName(name);
    queue.setConsumerCount(0);
    queue.setMessagesReady(messagesReady);
    return queue;
  }
}
