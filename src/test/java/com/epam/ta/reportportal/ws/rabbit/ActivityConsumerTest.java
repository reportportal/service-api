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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.epam.ta.reportportal.core.events.ActivityMessage;
import com.epam.ta.reportportal.dao.ActivityRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class ActivityConsumerTest {

  @Mock
  private ActivityRepository activityRepository;

  @InjectMocks
  private ActivityConsumer activityConsumer;

  @Test
  void nullPayloadIsIgnored() {
    activityConsumer.onEvent(null);
    verifyNoInteractions(activityRepository);
  }

  @Test
  void savedEventIsPersistedToRepository() {
    ActivityMessage message = new ActivityMessage();
    message.setSubjectId(1L);
    message.setProjectId(2L);
    message.setSubjectName("username");
    message.setObjectId(3L);
    message.setSavedEvent(true);
    message.setCreatedAt(Instant.now());

    activityConsumer.onEvent(message);

    verify(activityRepository, times(1)).save(any());
  }

  @Test
  void notSavedEventIsNotPersistedToRepository() {
    ActivityMessage message = new ActivityMessage();
    message.setSavedEvent(false);

    activityConsumer.onEvent(message);

    verifyNoInteractions(activityRepository);
  }

}
