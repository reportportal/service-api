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

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.entity.activity.Activity;
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

  private static class EmptyActivity implements ActivityEvent {

    @Override
    public Activity toActivity() {
      return null;
    }

    @Override
    public boolean isSavedEvent() {
      return false;
    }
  }

  @Test
  void nullTest() {
    activityConsumer.onEvent(new EmptyActivity().toActivity());
    verifyNoInteractions(activityRepository);
  }

  private static class NotEmptyActivity implements ActivityEvent {

    private final Long userId;
    private final Long projectId;
    private final String username;
    private final Long objectId;

    NotEmptyActivity(Long userId, Long projectId, String username, Long objectId) {
      this.userId = userId;
      this.projectId = projectId;
      this.username = username;
      this.objectId = objectId;
    }

    @Override
    public Activity toActivity() {
      Activity activity = new Activity();
      activity.setSubjectId(userId);
      activity.setProjectId(projectId);
      activity.setSubjectName(username);
      activity.setObjectId(objectId);
      return activity;
    }

    @Override
    public boolean isSavedEvent() {
      return true;
    }
  }

  @Test
  void consume() {
    NotEmptyActivity notEmptyActivity = new NotEmptyActivity(1L, 2L, "username", 3L);

    activityConsumer.onEvent(notEmptyActivity.toActivity());

    verify(activityRepository, times(1)).save(any());
  }
}
