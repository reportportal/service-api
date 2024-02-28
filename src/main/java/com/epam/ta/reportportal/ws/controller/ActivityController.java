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

 package com.epam.ta.reportportal.ws.controller;

 import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
 import static org.springframework.http.HttpStatus.OK;

 import com.epam.ta.reportportal.commons.EntityUtils;
 import com.epam.ta.reportportal.commons.ReportPortalUser;
 import com.epam.ta.reportportal.commons.querygen.Filter;
 import com.epam.ta.reportportal.core.activity.ActivityHandler;
 import com.epam.ta.reportportal.entity.activity.Activity;
 import com.epam.ta.reportportal.model.ActivityEventResource;
 import com.epam.ta.reportportal.util.ProjectExtractor;
 import com.epam.ta.reportportal.ws.model.ActivityResource;
 import com.epam.ta.reportportal.ws.resolver.FilterFor;
 import com.epam.ta.reportportal.ws.resolver.SortFor;
 import io.swagger.v3.oas.annotations.Operation;
 import io.swagger.v3.oas.annotations.tags.Tag;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Pageable;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.security.core.annotation.AuthenticationPrincipal;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.bind.annotation.GetMapping;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.bind.annotation.RestController;

 /**
  * @author Ihar_Kahadouski
  */
 @RestController
 @RequestMapping("/v1/{projectKey}/activity")
 @Transactional(readOnly = true)
 @PreAuthorize(ASSIGNED_TO_PROJECT)
 @Tag(name = "activity-controller", description = "Activity Controller")
 public class ActivityController {

   private final ActivityHandler activityHandler;

   private final ProjectExtractor projectExtractor;

   @Autowired
   public ActivityController(ActivityHandler activityHandler, ProjectExtractor projectExtractor) {
     this.activityHandler = activityHandler;
     this.projectExtractor = projectExtractor;
   }

   @GetMapping(value = "/{activityId}")
   @ResponseStatus(OK)
   public ActivityResource getActivity(@PathVariable String projectKey,
       @PathVariable Long activityId, @AuthenticationPrincipal ReportPortalUser user) {
     ReportPortalUser.ProjectDetails projectDetails =
         projectExtractor.extractProjectDetailsAdmin(user, EntityUtils.normalizeId(projectKey));
     return activityHandler.getActivity(projectDetails, activityId);
   }

   @GetMapping(value = "/item/{itemId}")
   @ResponseStatus(OK)
   @Operation(summary =  "Get activities for test item")
   public Iterable<ActivityEventResource> getTestItemActivities(@PathVariable String projectKey,
       @PathVariable Long itemId, @FilterFor(Activity.class) Filter filter,
       @SortFor(Activity.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
     ReportPortalUser.ProjectDetails projectDetails =
         projectExtractor.extractProjectDetailsAdmin(user, EntityUtils.normalizeId(projectKey));
     return activityHandler.getItemActivities(projectDetails, itemId, filter, pageable);
   }
 }
