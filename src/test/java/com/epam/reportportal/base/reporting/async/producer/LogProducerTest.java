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

package com.epam.reportportal.base.reporting.async.producer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.log.impl.SaveLogBinaryDataTaskAsync;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.reporting.SaveLogRQ;
import jakarta.inject.Provider;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

class LogProducerTest {

  @Test
  @SuppressWarnings("unchecked")
  void logIsStillPublishedWhenBinaryDataCannotBeSaved() {
    SaveLogBinaryDataTaskAsync task = mock(SaveLogBinaryDataTaskAsync.class);
    when(task.withRequest(any())).thenReturn(task);
    when(task.withFile(any())).thenReturn(task);
    when(task.withProjectId(any())).thenReturn(task);
    when(task.get()).thenThrow(new IllegalStateException("datastore is down"));
    Provider<SaveLogBinaryDataTaskAsync> provider = mock(Provider.class);
    when(provider.get()).thenReturn(task);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);

    LogProducer producer = new LogProducer();
    ReflectionTestUtils.setField(producer, "saveLogBinaryDataTask", provider);
    ReflectionTestUtils.setField(producer, "taskExecutor", (TaskExecutor) Runnable::run);
    ReflectionTestUtils.setField(producer, "rabbitTemplate", rabbitTemplate);

    SaveLogRQ request = new SaveLogRQ();
    request.setLaunchUuid("launch");
    MembershipDetails membership = mock(MembershipDetails.class);
    when(membership.getProjectId()).thenReturn(1L);

    var response = producer.createLog(request, new MockMultipartFile("file", new byte[] {1}),
        membership);

    assertEquals(request.getUuid(), response.getId());
    verify(rabbitTemplate).invoke(any());
  }
}
