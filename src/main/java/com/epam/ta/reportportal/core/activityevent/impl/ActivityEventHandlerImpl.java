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

import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.activityevent.ActivityEventHandler;
import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.ActivityEventConverter;
import com.epam.ta.reportportal.ws.model.ActivityEventResource;
import com.epam.ta.reportportal.ws.model.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Activity Event Handler Implementation.
 *
 * @author Ryhor_Kukharenka
 */
@Service
public class ActivityEventHandlerImpl implements ActivityEventHandler {

  private final ActivityRepository activityRepository;

  public ActivityEventHandlerImpl(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  @Override
  public PagedResponse<ActivityEventResource> getActivityEventsHistory(Queryable filter,
      Pageable pageable) {
    Page<Activity> activityPage = activityRepository.findByFilter(filter, pageable);
    return PagedResourcesAssembler
        .pagedResponseConverter(ActivityEventConverter.TO_RESOURCE)
        .apply(activityPage);
  }

}
