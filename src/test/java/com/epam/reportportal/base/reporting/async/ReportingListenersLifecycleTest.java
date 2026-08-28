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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;

class ReportingListenersLifecycleTest {

  @Test
  void republishesOnlyQueuesWhoseBindingsWereRemoved() {
    String unboundQueue = "q.reporting.current.0";
    String stillBoundQueue = "q.reporting.current.1";
    Binding removedBinding = binding(unboundQueue);
    Binding failedBinding = binding(stillBoundQueue);
    AmqpAdmin amqpAdmin = mock(AmqpAdmin.class);
    ReportingShovelService shovelService = mock(ReportingShovelService.class);
    AbstractMessageListenerContainer container = mock(AbstractMessageListenerContainer.class);
    doThrow(new IllegalStateException("unbind failed")).when(amqpAdmin)
        .removeBinding(failedBinding);
    when(amqpAdmin.getQueueInfo(unboundQueue))
        .thenReturn(new QueueInformation(unboundQueue, 1, 0));
    when(amqpAdmin.getQueueInfo(stillBoundQueue))
        .thenReturn(new QueueInformation(stillBoundQueue, 1, 0));
    ReportingListenersLifecycle lifecycle = new ReportingListenersLifecycle(List.of(container),
        List.of(removedBinding, failedBinding),
        List.of(new Queue(unboundQueue), new Queue(stillBoundQueue)), amqpAdmin, shovelService,
        Duration.ZERO);

    lifecycle.start();
    lifecycle.stop();

    verify(shovelService).republishToReportingExchange(unboundQueue);
    verify(shovelService, never()).republishToReportingExchange(stillBoundQueue);
  }

  private static Binding binding(String queue) {
    Binding binding = mock(Binding.class);
    when(binding.getDestination()).thenReturn(queue);
    when(binding.getExchange()).thenReturn("e.reporting");
    return binding;
  }
}
