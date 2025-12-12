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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.core.events.activity.converter.EventToActivityConverter;
import com.epam.reportportal.core.events.domain.AbstractEvent;
import com.epam.reportportal.core.events.domain.ProjectCreatedEvent;
import com.epam.reportportal.infrastructure.persistence.dao.ActivityRepository;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import java.util.Map;
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

  @Mock
  private Map<Class<? extends AbstractEvent<?>>, EventToActivityConverter<? extends AbstractEvent<?>>> converterMap;

  @InjectMocks
  private ActivityConsumer activityConsumer;

  @Test
  void nullTest() {
    activityConsumer.onEvent(null);
    verifyNoInteractions(activityRepository);
  }

  @Test
  void consume() {
    ProjectCreatedEvent event = new ProjectCreatedEvent(1L, "username", 2L, "Test Project", 1L);
    @SuppressWarnings("unchecked")
    EventToActivityConverter<ProjectCreatedEvent> converter = mock(EventToActivityConverter.class);
    Activity activity = new Activity();
    activity.setSubjectId(1L);
    activity.setProjectId(2L);
    activity.setSubjectName("username");
    activity.setObjectId(3L);
    activity.setSavedEvent(true);

    @SuppressWarnings("rawtypes")
    EventToActivityConverter wildcardConverter = converter;
    when(converterMap.get(event.getClass())).thenReturn(wildcardConverter);
    when(converter.convert(event)).thenReturn(activity);

    activityConsumer.onEvent(event);

    verify(activityRepository, times(1)).save(any(Activity.class));
  }

  @Test
  void consumeWhenNoConverter() {
    ProjectCreatedEvent event = new ProjectCreatedEvent(1L, "username", 2L, "Test Project", 1L);

    when(converterMap.get(event.getClass())).thenReturn(null);

    activityConsumer.onEvent(event);

    verifyNoInteractions(activityRepository);
  }

  @Test
  void consumeWhenActivityIsNull() {
    ProjectCreatedEvent event = new ProjectCreatedEvent(1L, "username", 2L, "Test Project", 1L);
    @SuppressWarnings("unchecked")
    EventToActivityConverter<ProjectCreatedEvent> converter = mock(EventToActivityConverter.class);

    @SuppressWarnings("rawtypes")
    EventToActivityConverter wildcardConverter = converter;
    when(converterMap.get(event.getClass())).thenReturn(wildcardConverter);
    when(converter.convert(event)).thenReturn(null);

    activityConsumer.onEvent(event);

    verifyNoInteractions(activityRepository);
  }

  @Test
  void consumeWhenActivityIsNotSavedEvent() {
    ProjectCreatedEvent event = new ProjectCreatedEvent(1L, "username", 2L, "Test Project", 1L);
    @SuppressWarnings("unchecked")
    EventToActivityConverter<ProjectCreatedEvent> converter = mock(EventToActivityConverter.class);
    Activity activity = new Activity();
    activity.setSubjectId(1L);
    activity.setProjectId(2L);
    activity.setSubjectName("username");
    activity.setObjectId(3L);
    activity.setSavedEvent(false);

    @SuppressWarnings("rawtypes")
    EventToActivityConverter wildcardConverter = converter;
    when(converterMap.get(event.getClass())).thenReturn(wildcardConverter);
    when(converter.convert(event)).thenReturn(activity);

    activityConsumer.onEvent(event);

    verifyNoInteractions(activityRepository);
  }
}
