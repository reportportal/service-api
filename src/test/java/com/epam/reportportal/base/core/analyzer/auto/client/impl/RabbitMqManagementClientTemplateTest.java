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

package com.epam.reportportal.base.core.analyzer.auto.client.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.BindingInfo;
import com.rabbitmq.http.client.domain.DestinationType;
import com.rabbitmq.http.client.domain.QueueInfo;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RabbitMqManagementClientTemplateTest {

  private static final String VHOST = "analyzer";

  @Mock
  private Client rabbitClient;

  private RabbitMqManagementClientTemplate template;

  @BeforeEach
  void setUp() {
    template = new RabbitMqManagementClientTemplate(rabbitClient, VHOST);
  }

  @Test
  void getAnalyzerExchangesInfoShouldThrowWhenExchangesAreNull() {
    // given
    when(rabbitClient.getExchanges(VHOST)).thenReturn(null);

    // when / then
    assertThrows(ReportPortalException.class, () -> template.getAnalyzerExchangesInfo());
  }

  @Test
  void getExchangesWithActiveConsumersShouldReturnExchangeWhenBoundQueueHasConsumers() {
    // given
    when(rabbitClient.getQueues(VHOST)).thenReturn(List.of(queue(1)));
    when(rabbitClient.getBindings(VHOST)).thenReturn(List.of(queueBinding()));

    // when
    Set<String> result = template.getExchangesWithActiveConsumers();

    // then
    assertEquals(Set.of("analyzer"), result);
  }

  @Test
  void getExchangesWithActiveConsumersShouldReturnEmptyWhenNoQueueHasConsumers() {
    // given
    when(rabbitClient.getQueues(VHOST)).thenReturn(List.of(queue(0)));

    // when
    Set<String> result = template.getExchangesWithActiveConsumers();

    // then
    assertTrue(result.isEmpty());
  }

  @Test
  void getExchangesWithActiveConsumersShouldReturnEmptyWhenConsumingQueueIsNotBound() {
    // given
    when(rabbitClient.getQueues(VHOST)).thenReturn(List.of(queue(1)));
    when(rabbitClient.getBindings(VHOST)).thenReturn(List.of());

    // when
    Set<String> result = template.getExchangesWithActiveConsumers();

    // then
    assertTrue(result.isEmpty());
  }

  private static QueueInfo queue(int consumerCount) {
    QueueInfo queue = new QueueInfo();
    queue.setName("all");
    queue.setConsumerCount(consumerCount);
    return queue;
  }

  private static BindingInfo queueBinding() {
    BindingInfo binding = new BindingInfo();
    binding.setSource("analyzer");
    binding.setDestinationType(DestinationType.QUEUE);
    binding.setDestination("all");
    return binding;
  }
}
