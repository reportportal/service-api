/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.epam.ta.reportportal.core.activityevent.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.ws.model.ActivityEventResource;
import com.epam.ta.reportportal.ws.model.PagedResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@ExtendWith(MockitoExtension.class)
class ActivityEventHandlerTest {


  @Mock
  private ActivityRepository activityRepository;

  @InjectMocks
  private ActivityEventHandlerImpl activityEventHandler;

  @Test
  void testGetActivityEventsHistory() {
    Queryable filter = mock(Queryable.class);
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Direction.ASC, "id"));

    List<Activity> sampleActivities = List.of(createActivity(1L), createActivity(2L));
    Page<Activity> activityPage =
        new PageImpl<>(sampleActivities, pageable, sampleActivities.size());

    when(activityRepository.findByFilter(filter, pageable)).thenReturn(activityPage);

    PagedResponse<ActivityEventResource> pagedResponse =
        activityEventHandler.getActivityEventsHistory(filter, pageable);

    assertEquals(activityPage.getPageable().getOffset(), pagedResponse.getOffset());
    assertEquals(activityPage.getSize(), pagedResponse.getLimit());
    assertEquals(activityPage.getNumber(), pagedResponse.getOffset() / pagedResponse.getLimit());
    assertEquals(activityPage.getTotalElements(), pagedResponse.getTotalCount());
  }

  private Activity createActivity(Long id) {
    final Activity activity = new Activity();
    activity.setId(id);
    activity.setCreatedAt(LocalDateTime.of(2023, 7, 4, 23, 30));
    activity.setAction(EventAction.CREATE);
    activity.setEventName("createDashboard");
    activity.setPriority(EventPriority.LOW);
    activity.setObjectId(1L);
    activity.setObjectName("objectName");
    activity.setObjectType(EventObject.DASHBOARD);
    activity.setProjectId(1L);
    activity.setProjectName("projectName");
    activity.setDetails(new ActivityDetails());
    activity.setSubjectId(1L);
    activity.setSubjectName("superadmin");
    activity.setSubjectType(EventSubject.USER);
    return activity;
  }

}