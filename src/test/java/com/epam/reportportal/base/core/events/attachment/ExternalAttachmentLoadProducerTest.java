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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ExternalAttachmentLoadProducerTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @Test
  void publishesApplicationEvent() {
    ExternalAttachmentLoadProducer producer =
        new ExternalAttachmentLoadProducer(applicationEventPublisher);

    producer.publish("mobitru", "loadExternalAttachment", 123L, 456L, 789L, 100L, "device-1",
        "mobitru_mobile_recording_id");

    ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
    verify(applicationEventPublisher).publishEvent(payloadCaptor.capture());

    assertThat(payloadCaptor.getValue()).isInstanceOf(ExternalAttachmentLoadEvent.class);
    ExternalAttachmentLoadEvent event = (ExternalAttachmentLoadEvent) payloadCaptor.getValue();
    assertThat(event.getPluginId()).isEqualTo("mobitru");
    assertThat(event.getPluginCommandName()).isEqualTo("loadExternalAttachment");
    assertThat(event.getLogId()).isEqualTo(123L);
    assertThat(event.getProjectId()).isEqualTo(456L);
    assertThat(event.getLaunchId()).isEqualTo(789L);
    assertThat(event.getTestItemId()).isEqualTo(100L);
    assertThat(event.getAttachmentExternalId()).isEqualTo("device-1");
    assertThat(event.getAttachmentAttributeKey()).isEqualTo("mobitru_mobile_recording_id");
  }
}
