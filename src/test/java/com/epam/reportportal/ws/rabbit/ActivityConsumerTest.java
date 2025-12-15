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

package com.epam.reportportal.ws.rabbit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.core.events.activity.converter.EventConverterRegistry;
import com.epam.reportportal.core.events.domain.ProjectCreatedEvent;
import com.epam.reportportal.core.events.widget.GenerateWidgetViewEvent;
import com.epam.reportportal.infrastructure.persistence.dao.ActivityRepository;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityDetails;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

  @Mock
  private EventConverterRegistry converterRegistry;

  @InjectMocks
  private ActivityConsumer activityConsumer;

  @Test
  void onEventWhenEventIsNullThenNoProcessing() {
    // given & when
    activityConsumer.onEvent(null);

    // then
    verifyNoInteractions(activityRepository);
    verifyNoInteractions(converterRegistry);
  }

  @Test
  void onEventWhenConverterReturnsActivityWithNullDetailsThenCreatesDetailsAndSaves() {
    // given
    ProjectCreatedEvent event = new ProjectCreatedEvent(1L, "username", 2L, "Test Project", 1L);
    Activity activity = new Activity();
    activity.setSubjectId(1L);
    activity.setProjectId(2L);
    activity.setSubjectName("username");
    activity.setObjectId(3L);
    activity.setSavedEvent(true);
    activity.setDetails(null);

    when(converterRegistry.convert(event)).thenReturn(Optional.of(activity));

    // when
    activityConsumer.onEvent(event);

    // then
    ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
    verify(activityRepository, times(1)).save(activityCaptor.capture());
    Activity savedActivity = activityCaptor.getValue();
    assertNotNull(savedActivity.getDetails());
  }

  @Test
  void onEventWhenConverterReturnsActivityWithExistingDetailsThenSavesWithExistingDetails() {
    // given
    ProjectCreatedEvent event = new ProjectCreatedEvent(1L, "username", 2L, "Test Project", 1L);
    Activity activity = new Activity();
    activity.setSubjectId(1L);
    activity.setProjectId(2L);
    activity.setSubjectName("username");
    activity.setObjectId(3L);
    activity.setSavedEvent(true);
    ActivityDetails existingDetails = new ActivityDetails();
    activity.setDetails(existingDetails);

    when(converterRegistry.convert(event)).thenReturn(Optional.of(activity));

    // when
    activityConsumer.onEvent(event);

    // then
    ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
    verify(activityRepository, times(1)).save(activityCaptor.capture());
    Activity savedActivity = activityCaptor.getValue();
    assertSame(existingDetails, savedActivity.getDetails());
  }

  @Test
  void onEventWhenNoConverterExistsThenNoProcessing() {
    // given
    GenerateWidgetViewEvent event = new GenerateWidgetViewEvent(1L);
    when(converterRegistry.convert(event)).thenReturn(Optional.empty());

    // when
    activityConsumer.onEvent(event);

    // then
    verifyNoInteractions(activityRepository);
  }

  @Test
  void onEventWhenActivityIsNotSavedEventThenNoProcessing() {
    // given
    ProjectCreatedEvent event = new ProjectCreatedEvent(1L, "username", 2L, "Test Project", 1L);
    Activity activity = new Activity();
    activity.setSubjectId(1L);
    activity.setProjectId(2L);
    activity.setSubjectName("username");
    activity.setObjectId(3L);
    activity.setSavedEvent(false);

    when(converterRegistry.convert(event)).thenReturn(Optional.of(activity));

    // when
    activityConsumer.onEvent(event);

    // then
    verifyNoInteractions(activityRepository);
  }
}
