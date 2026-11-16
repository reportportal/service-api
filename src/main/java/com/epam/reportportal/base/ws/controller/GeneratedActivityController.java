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

package com.epam.reportportal.base.ws.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;
import static com.epam.reportportal.base.auth.permissions.Permissions.IS_ADMIN;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_CREATED_AT;
import static org.springframework.data.domain.Sort.Direction.ASC;

import com.epam.reportportal.api.ActivitiesApi;
import com.epam.reportportal.api.model.ActivitiesPage;
import com.epam.reportportal.api.model.ActivitiesSearch200Response;
import com.epam.reportportal.api.model.ProjectActivity;
import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.reportportal.base.core.activity.ActivityHandler;
import com.epam.reportportal.base.core.activityevent.ActivityEventHandler;
import com.epam.reportportal.base.core.filter.SearchCriteriaService;
import com.epam.reportportal.base.core.filter.predefined.PredefinedFilterType;
import com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.model.ActivityEventResource;
import com.epam.reportportal.base.util.ControllerUtils;
import com.epam.reportportal.base.util.OffsetUtils;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.epam.reportportal.base.ws.converter.converters.ActivityConverter;
import com.epam.reportportal.base.ws.converter.converters.ActivityEventConverter;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling activity-related operations. Implements the {@link ActivitiesApi} interface.
 */
@RestController
@RequiredArgsConstructor
public class GeneratedActivityController implements ActivitiesApi {

  private final ActivityEventHandler activityEventHandler;
  private final SearchCriteriaService searchCriteriaService;
  private final ActivityHandler activityHandler;
  private final ProjectExtractor projectExtractor;

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

  @Override
  @Transactional(readOnly = true)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public ResponseEntity<ProjectActivity> getProjectActivity(String projectKey, Long activityId) {
    MembershipDetails membershipDetails = extractMembershipDetails(projectKey);
    var activity = activityHandler.getActivity(membershipDetails, activityId);
    return ResponseEntity.ok(ActivityConverter.TO_PROJECT_ACTIVITY_API_MODEL.apply(activity));
  }

  @Override
  @Transactional(readOnly = true)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public ResponseEntity<ActivitiesPage> getTestItemActivities(String projectKey, Long itemId, Integer offset,
      Integer limit, String order, String sort) {
    MembershipDetails membershipDetails = extractMembershipDetails(projectKey);
    Pageable pageable = ControllerUtils.getPageable(sort, order, offset, limit);
    var page = activityHandler.getItemActivities(membershipDetails, itemId, emptyActivityFilter(), pageable);
    return ResponseEntity.ok(toActivitiesPage(page, pageable));
  }

  @Override
  @Transactional(readOnly = true)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public ResponseEntity<ActivitiesPage> getTestCaseActivities(String projectKey, Long testCaseId, Integer offset,
      Integer limit, String order, String sort) {
    MembershipDetails membershipDetails = extractMembershipDetails(projectKey);
    Pageable pageable = ControllerUtils.getPageable(sort, order, offset, limit);
    var page = activityHandler.getTestCaseActivities(membershipDetails, testCaseId, emptyActivityFilter(), pageable);
    return ResponseEntity.ok(toActivitiesPage(page, pageable));
  }

  private MembershipDetails extractMembershipDetails(String projectKey) {
    return projectExtractor.extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey));
  }

  private Filter emptyActivityFilter() {
    return new Filter(Activity.class, new ArrayList<>());
  }

  private ActivitiesPage toActivitiesPage(com.epam.reportportal.base.model.Page<ActivityEventResource> page,
      Pageable pageable) {
    var items = page.getContent().stream().map(ActivityEventConverter.TO_API_MODEL).toList();
    return OffsetUtils.responseWithPageParameters(new ActivitiesPage().items(items), pageable,
        page.getPage().getTotalElements());
  }

}
