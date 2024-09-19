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

import com.epam.reportportal.model.ActivityResource;
import com.epam.reportportal.rules.exception.ErrorRS;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.activity.ActivityHandler;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.model.ActivityEventResource;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Ihar_Kahadouski
 */
@RestController
@RequestMapping("/v1/{projectName}/activity")
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

  @RequestMapping(value = "/{activityId}", method = RequestMethod.GET)
  @ResponseStatus(OK)
  @Operation(summary = "Get an activity by its ID in a specific project", description = """
      Fetches the activity details by its ID for a specific project.""", responses = {
      @ApiResponse(responseCode = "200", description = "Successful operation. Returns the "
          + "Activity", content = @Content(schema = @Schema(implementation =
          ActivityResource.class), examples = @ExampleObject(name = "Successful operation",
          value = """
          {
                "id": 1,
                "user": "superadmin",
                "loggedObjectId": 22,
                "lastModified": "2024-03-29T15:03:54.156904Z",
                "actionType": "finishLaunch",
                "objectType": "LAUNCH",
                "projectId": 1,
                "details": {
                    "history": []
                },
                "objectName": "1 step (failed) - filled description"
          }
          """))),
      @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema =
      @Schema(implementation = ErrorRS.class), examples = @ExampleObject(name = "Bad request",
          value = """
          {
              "timestamp": "2024-05-20T07:12:15.698+00:00",
              "status": 400,
              "error": "Bad Request",
              "path": "/v1/superadmin_personal/activity/1test"
          }
          """))),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema
          = @Schema(implementation = ErrorRS.class), examples = @ExampleObject(name =
          "Unauthorized", value = """
          {
              "error": "unauthorized",
              "error_description": "Full authentication is required to access this resource."
          }
          """))),
      @ApiResponse(responseCode = "403", description = "Access Denied", content =
      @Content(schema = @Schema(implementation = ErrorRS.class), examples = @ExampleObject(name =
          "Access Denied", value = """
          {
              "error": "access_denied",
              "error_description": "You do not have enough permissions"
          }
          """))),
      @ApiResponse(responseCode = "404", description = "Activity not found", content =
      @Content(schema = @Schema(implementation = ErrorRS.class), examples = @ExampleObject(name =
          "Activity not found", value = """
          {
              "errorCode": 40411,
              "message": "Activity '1' not found. Did you use correct Activity ID?"
          }
          """))),
      @ApiResponse(responseCode = "500", description = "Internal server error", content =
      @Content(schema = @Schema(implementation = ErrorRS.class), examples = @ExampleObject(name =
          "Unclassified error", value = """
          {
              "errorCode": 5000,
              "message": "Unclassified error"
          }
          """))) })
  public ActivityResource getActivity(@PathVariable
  @Parameter(description = "The name of the project for which the activity should be searched")
  String projectName,
      @PathVariable @Parameter(description = "The ID of the activity to be searched")
      Long activityId, @AuthenticationPrincipal ReportPortalUser user) {
    ReportPortalUser.ProjectDetails projectDetails =
        projectExtractor.extractProjectDetailsAdmin(user, EntityUtils.normalizeId(projectName));
    return activityHandler.getActivity(projectDetails, activityId);
  }

  @RequestMapping(value = "/item/{itemId}", method = RequestMethod.GET)
  @ResponseStatus(OK)
  @Operation(summary = "Get a list of item activities for a specific project", description = """
      Fetches a list of item activities for a specific project.
      <p>Action field can have these values:</p>
      <ul>
      <li>'create'</li>
      <li>'update'</li>
      <li>'delete'</li>
      <li>'bulkCreate'</li>
      <li>'bulkUpdate'</li>
      <li>'bulkDelete'</li>
      <li>'analyze'</li>
      <li>'start'</li>
      <li>'finish'</li>
      <li>'post'</li>
      <li>'link'</li>
      <li>'unlink'</li>
      <li>'assign'</li>
      <li>'unassign'</li>
      <li>'generate'</li>
      <li>'match'</li>
      <li>'changeRole'</li>
      <li>'updateUserRole'</li>
      </ul>
      <p>Priority field can have these values:</p>
      <ul>
      <li>'critical'</li>
      <li>'high'</li>
      <li>'medium'</li>
      <li>'low'</li>
      <li>'info'</li>
      </ul>
      <p>Object type field can have these values:</p>
      <ul>
      <li>'launch'</li>
      <li>'dashboard'</li>
      <li>'defectType'</li>
      <li>'emailConfig'</li>
      <li>'filter'</li>
      <li>'import'</li>
      <li>'integration'</li>
      <li>'itemIssue'</li>
      <li>'project'</li>
      <li>'sharing'</li>
      <li>'user'</li>
      <li>'widget'</li>
      <li>'pattern'</li>
      <li>'index'</li>
      <li>'plugin'</li>
      <li>'invitationLink'</li>
      </ul>
      <p>Subject type field can have these values:</p>
      <ul>
      <li>'user'</li>
      <li>'application'</li>
      <li>'rule'</li>
      <li>'custom'</li>
      </ul>
      <p> You can filter by different operators:
      <ul>
      <li>eq (Equals condition)</li>
      <li>ne (Not equals condition)</li>
      <li>cnt (Contains condition)</li>
      <li>under</li>
      <li>level (Number of labels in path)</li>
      <li>ex (Exists condition)</li>
      <li>in (Accepts filter value as comma-separated list)</li>
      <li>ea (Equals any. Accepts filter value as comma-separated list)</li>
      <li>has (Accepts filter value as comma-separated list. Returns 'TRUE' of all
      provided values exist in collection)</li>
      <li>any (Overlap condition between two arrays)</li>
      <li>gt (Greater than condition)</li>
      <li>gte (Greater than or Equals condition)</li>
      <li>lt (Lower than condition)</li>
      <li>lte (Lower than or Equals condition)</li>
      <li>btw (Between condition. Include boundaries. 'Between' condition applicable only for
      positive Numbers, Dates or specific TimeStamp values)</li>
      </ul>
      """, responses = {
      @ApiResponse(responseCode = "200", description = "Successful operation. Returns a list of "
          + "activities.", content = @Content(schema = @Schema(implementation = Page.class),
          examples = @ExampleObject(name = "Successful operation", value = """
          {
               "content": [
                   {
                       "id": 1,
                       "created_at": "2024-03-29T12:29:54.772Z",
                       "event_name": "updateItem",
                       "object_id": 1153,
                       "object_name": "Step-1(A)",
                       "object_type": "itemIssue",
                       "project_id": 1,
                       "project_name": "superadmin_personal",
                       "subject_name": "superadmin",
                       "subject_type": "user",
                       "subject_id": "1",
                       "details": {
                           "history": [
                               {
                                   "field": "status",
                                   "oldValue": "SKIPPED",
                                   "newValue": "FAILED"
                               }
                           ]
                       }
                   }
               ],
               "page": {
                   "number": 1,
                   "size": 20,
                   "totalElements": 1,
                   "totalPages": 1
               }
           }
          """))),
      @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema =
      @Schema(implementation = ErrorRS.class), examples = @ExampleObject(name = "Bad request",
          value = """
          {
              "timestamp": "2024-05-20T07:03:53.007+00:00",
              "status": 400,
              "error": "Bad Request",
              "path": "/v1/superadmin_personal/activity/item/1test"
          }
          """))),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema
          = @Schema(implementation = ErrorRS.class), examples = @ExampleObject(name =
          "Unauthorized", value = """
          {
              "error": "unauthorized",
              "error_description": "Full authentication is required to access this resource."
          }
          """))),
      @ApiResponse(responseCode = "403", description = "Access Denied", content =
      @Content(schema = @Schema(implementation = ErrorRS.class), examples = @ExampleObject(name =
          "Access Denied", value = """
          {
              "error": "access_denied",
              "error_description": "You do not have enough permissions"
          }
          """))),
      @ApiResponse(responseCode = "404", description = "Test item not found or Launch not found",
          content = @Content(schema = @Schema(implementation = ErrorRS.class), examples =
          @ExampleObject(name = "Test item not found", value = """
          {
              "errorCode": 4043,
              "message": "Test Item '1' not found. Did you use correct Test Item ID?"
          }
          """))),
      @ApiResponse(responseCode = "500", description = "Internal server error", content =
      @Content(schema = @Schema(implementation = ErrorType.class), examples =
      @ExampleObject(name = "Unclassified error", value = """
          {
              "errorCode": 5000,
              "message": "Unclassified error"
          }
          """))) })
  public Iterable<ActivityEventResource> getTestItemActivities(@PathVariable
  @Parameter(description = "The name of the project for which the activities should be searched")
  String projectName, @PathVariable
  @Parameter(description = "The ID of the test item for which all its activities should be "
      + "searched") Long itemId, @FilterFor(Activity.class) Filter filter,
      @SortFor(Activity.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
    ReportPortalUser.ProjectDetails projectDetails =
        projectExtractor.extractProjectDetailsAdmin(user, EntityUtils.normalizeId(projectName));
    return activityHandler.getItemActivities(projectDetails, itemId, filter, pageable);
  }
}