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

package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;
import static com.epam.ta.reportportal.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_CREATED_AT;
import static org.springframework.data.domain.Sort.Direction.ASC;

import com.epam.reportportal.api.ActivitiesApi;
import com.epam.reportportal.api.model.ActivitiesSearch200Response;
import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.activityevent.ActivityEventHandler;
import com.epam.ta.reportportal.core.filter.SearchCriteriaService;
import com.epam.ta.reportportal.core.filter.predefined.PredefinedFilterType;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.util.ControllerUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling activity event search operations. Implements the {@link ActivitiesApi} interface.
 */
@RestController
public class GeneratedActivityController implements ActivitiesApi {

  private final ActivityEventHandler activityEventHandler;
  private final SearchCriteriaService searchCriteriaService;


  public GeneratedActivityController(ActivityEventHandler activityEventHandler,
      SearchCriteriaService searchCriteriaService) {
    this.activityEventHandler = activityEventHandler;
    this.searchCriteriaService = searchCriteriaService;
  }

  @Override
  @Transactional(readOnly = true)
  @PreAuthorize(IS_ADMIN)
  public ResponseEntity<ActivitiesSearch200Response> activitiesSearch(SearchCriteriaRQ searchCriteria) {
    Queryable filter = searchCriteriaService.createFilterBySearchCriteria(searchCriteria, Activity.class,
        PredefinedFilterType.ACTIVITIES
    );
    var pageable = ControllerUtils.getPageable(
        StringUtils.isNotBlank(searchCriteria.getSort()) ? searchCriteria.getSort() : CRITERIA_CREATED_AT,
        searchCriteria.getOrder() != null ? searchCriteria.getOrder().toString() : ASC.toString(),
        searchCriteria.getOffset(),
        searchCriteria.getLimit());

    return ResponseEntity.ok(activityEventHandler.getActivityEventsHistory(filter, pageable));
  }

}
