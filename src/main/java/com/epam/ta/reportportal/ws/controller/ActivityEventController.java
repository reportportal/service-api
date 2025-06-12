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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.core.activityevent.ActivityEventHandler;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Activity Event Controller.
 *
 * @author Ryhor_Kukharenka
 */
@Validated
@RestController
@RequestMapping("/v1/activities")
@Tag(name = "Activity Event", description = "Activity Events API collection")
public class ActivityEventController {

  private final ActivityEventHandler activityEventHandler;
  private final ProjectExtractor projectExtractor;

  public ActivityEventController(ActivityEventHandler activityEventHandler, ProjectExtractor projectExtractor) {
    this.activityEventHandler = activityEventHandler;
    this.projectExtractor = projectExtractor;
  }


  @GetMapping("/{projectKey}/subjectName")
  @PreAuthorize(IS_ADMIN)
  @Operation(summary = "Load project activities subjectNames by filter", description = "Only for current project")
  public List<String> getProjectSubjectName(@PathVariable String projectKey,
      @RequestParam(FilterCriteriaResolver.DEFAULT_FILTER_PREFIX + Condition.CNT + "subjectName")
      String value, @AuthenticationPrincipal ReportPortalUser user) {
    return activityEventHandler.getSubjectNames(projectExtractor.extractMembershipDetails(user, projectKey), value);
  }
}
