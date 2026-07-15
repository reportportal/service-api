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

package com.epam.reportportal.base.core.events.attachment;

import static com.epam.reportportal.base.core.configs.rabbit.InternalConfiguration.EXCHANGE_ATTACHMENT;
import static com.epam.reportportal.base.core.configs.rabbit.InternalConfiguration.QUEUE_ATTACHMENT_EXTERNAL_LOAD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.transaction.event.TransactionalEventListener;

@ExtendWith(MockitoExtension.class)
class ExternalAttachmentLoadRabbitPublisherTest {

  @Mock
  private AmqpTemplate amqpTemplate;

  @Test
  void publishesEventToAttachmentExchangeWithExternalLoadRoutingKey() {
    ExternalAttachmentLoadRabbitPublisher publisher =
        new ExternalAttachmentLoadRabbitPublisher(amqpTemplate);
    ExternalAttachmentLoadEvent event = new ExternalAttachmentLoadEvent("mobitru",
        "loadExternalAttachment", 123L, 456L, 789L, 100L, "device-1",
        "mobitru_mobile_recording_id");

    publisher.onApplicationEvent(event);

    verify(amqpTemplate).convertAndSend(EXCHANGE_ATTACHMENT, QUEUE_ATTACHMENT_EXTERNAL_LOAD, event);
  }

  @Test
  void publishesAfterCommitAndFallsBackWithoutTransaction() throws NoSuchMethodException {
    Method listenerMethod = ExternalAttachmentLoadRabbitPublisher.class.getDeclaredMethod(
        "onApplicationEvent", ExternalAttachmentLoadEvent.class);

    TransactionalEventListener annotation =
        listenerMethod.getAnnotation(TransactionalEventListener.class);

    assertThat(annotation).isNotNull();
    assertThat(annotation.phase()).isEqualTo(AFTER_COMMIT);
    assertThat(annotation.fallbackExecution()).isTrue();
  }
}
