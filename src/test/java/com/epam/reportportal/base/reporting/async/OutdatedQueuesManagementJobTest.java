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

package com.epam.reportportal.base.reporting.async;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.HttpClientException;
import com.rabbitmq.http.client.domain.DeleteQueueParameters;
import com.rabbitmq.http.client.domain.QueueInfo;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Queue;

class OutdatedQueuesManagementJobTest {

  private static final String VHOST = "analyzer";
  private static final String QUEUE = "q.reporting.pod-a.0";

  private final Client client = mock(Client.class);
  private final ReportingShovelService shovelService = mock(ReportingShovelService.class);
  private final OutdatedQueuesManagementJob job = new OutdatedQueuesManagementJob(client,
      shovelService, List.of(new Queue("q.reporting.own.0")), VHOST, Duration.ZERO);

  private QueueInfo queue(long consumers, long messagesReady) {
    QueueInfo info = new QueueInfo();
    info.setName(QUEUE);
    info.setConsumerCount(consumers);
    info.setMessagesReady(messagesReady);
    return info;
  }

  @Test
  void queueThatGotConsumersBeforeUnbindIsLeftAlone() {
    when(client.getQueues(VHOST)).thenReturn(List.of(queue(0, 0)));
    when(client.getQueue(VHOST, QUEUE)).thenReturn(queue(3, 0));

    job.run();

    verify(client, never()).unbindQueue(anyString(), anyString(), anyString(), anyString());
    verify(client, never()).deleteQueue(anyString(), anyString());
    verify(client, never()).deleteQueue(anyString(), anyString(), any());
  }

  @Test
  void emptyQueueIsDeletedOnlyWhenEmptyAndUnused() {
    when(client.getQueues(VHOST)).thenReturn(List.of(queue(0, 0)));
    when(client.getQueue(VHOST, QUEUE)).thenReturn(queue(0, 0));

    job.run();

    ArgumentCaptor<DeleteQueueParameters> params =
        ArgumentCaptor.forClass(DeleteQueueParameters.class);
    verify(client).deleteQueue(eq(VHOST), eq(QUEUE), params.capture());
    assertTrue(params.getValue().isIfEmpty());
    assertTrue(params.getValue().isIfUnused());
    verify(client, never()).deleteQueue(VHOST, QUEUE);
  }

  @Test
  void preconditionFailureOnDeleteIsRetriedNextRun() {
    when(client.getQueues(VHOST)).thenReturn(List.of(queue(0, 0)));
    when(client.getQueue(VHOST, QUEUE)).thenReturn(queue(0, 0));
    doThrow(new HttpClientException(400, "PRECONDITION_FAILED")).when(client)
        .deleteQueue(eq(VHOST), eq(QUEUE), any(DeleteQueueParameters.class));

    assertDoesNotThrow(job::run);
    assertDoesNotThrow(job::run);

    verify(client, times(2)).deleteQueue(eq(VHOST), eq(QUEUE), any(DeleteQueueParameters.class));
  }
}
