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

package com.epam.reportportal.base.reporting.async.config;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

class ReportingTopologyConfigurationTest {

  private final AmqpAdmin amqpAdmin = mock(AmqpAdmin.class);
  private final ReportingTopologyConfiguration configuration =
      new ReportingTopologyConfiguration(amqpAdmin, mock(ApplicationContext.class));

  @Test
  void createsDeclarablesWithoutDeclaringThemImperatively() {
    ReflectionTestUtils.setField(configuration, "queuesCount", 1);

    List<Queue> queues = configuration.reportingQueues();
    List<Binding> bindings = configuration.reportingBindings(queues);

    assertTrue(queues.getFirst().getName().matches("q\\.reporting\\.[a-zA-Z0-9._-]+\\.0"));
    assertTrue(bindings.getFirst().isDestinationQueue());
    verifyNoInteractions(amqpAdmin);
  }

  @Test
  void createsUniqueQueueNamesForSeparateInstances() {
    ReflectionTestUtils.setField(configuration, "queuesCount", 1);

    String firstQueue = configuration.reportingQueues().getFirst().getName();
    String secondQueue = configuration.reportingQueues().getFirst().getName();

    assertNotEquals(firstQueue, secondQueue);
  }
}
