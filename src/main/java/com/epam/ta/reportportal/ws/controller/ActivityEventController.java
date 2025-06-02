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

package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;
import static com.epam.ta.reportportal.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_CREATED_AT;
import static org.springframework.data.domain.Sort.Direction.ASC;

import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.activityevent.ActivityEventHandler;
import com.epam.ta.reportportal.core.filter.SearchCriteriaService;
import com.epam.ta.reportportal.core.filter.predefined.PredefinedFilterType;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.model.ActivityEventResource;
import com.epam.ta.reportportal.model.PagedResponse;
import com.epam.ta.reportportal.util.ControllerUtils;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Activity Event Controller.
 *
 * @author Ryhor_Kukharenka
 */
@Validated
@RestController
@Tag(name = "Activity Event", description = "Activity Events API collection")
public class ActivityEventController {

  private final ActivityEventHandler activityEventHandler;
  private final ProjectExtractor projectExtractor;
  private final SearchCriteriaService searchCriteriaService;

  public ActivityEventController(ActivityEventHandler activityEventHandler,
      ProjectExtractor projectExtractor, SearchCriteriaService searchCriteriaService) {
    this.activityEventHandler = activityEventHandler;
    this.projectExtractor = projectExtractor;
    this.searchCriteriaService = searchCriteriaService;
  }

  /**
   * Get activities by search criteria.
   *
   * @param limit          Limit
   * @param offset         Offset
   * @param order          Order by
   * @param sort           Sort by
   * @param searchCriteria Search criteria
   * @return Event Activity Page
   */
  @PreAuthorize(IS_ADMIN)
  @PostMapping("/activities/searches")
  @Operation(summary = "Get activities by search criteria")
  public PagedResponse<ActivityEventResource> getActivities(
      @RequestParam @Min(0) @Max(300) int limit, @RequestParam @Min(0) int offset,
      @RequestParam Direction order, @RequestParam String sort,
      @RequestBody SearchCriteriaRQ searchCriteria) {

    Queryable filter =
        searchCriteriaService.createFilterBySearchCriteria(searchCriteria, Activity.class,
            PredefinedFilterType.ACTIVITIES
        );
    var pageable = ControllerUtils.getPageable(
        StringUtils.isNotBlank(sort) ? sort : CRITERIA_CREATED_AT,
        order != null ? order.toString() : ASC.toString(),
        offset,
        limit);

    return activityEventHandler.getActivityEventsHistory(filter, pageable);
  }

  @GetMapping("/v1/activities/{projectKey}/subjectName")
  @PreAuthorize(IS_ADMIN)
  @Operation(summary =  "Load project activities subjectNames by filter", description = "Only for current project")
  public List<String> getProjectSubjectName(@PathVariable String projectKey,
      @RequestParam(FilterCriteriaResolver.DEFAULT_FILTER_PREFIX + Condition.CNT + "subjectName")
      String value, @AuthenticationPrincipal ReportPortalUser user) {
    return activityEventHandler.getSubjectNames(
        projectExtractor.extractMembershipDetails(user, projectKey), value);
  }
}
