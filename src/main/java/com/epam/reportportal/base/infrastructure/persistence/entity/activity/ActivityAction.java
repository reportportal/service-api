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

package com.epam.reportportal.base.infrastructure.persistence.entity.activity;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public enum ActivityAction {

  CREATE_DASHBOARD("createDashboard"),
  UPDATE_DASHBOARD("updateDashboard"),
  DELETE_DASHBOARD("deleteDashboard"),
  CREATE_WIDGET("createWidget"),
  UPDATE_WIDGET("updateWidget"),
  DELETE_WIDGET("deleteWidget"),
  CREATE_FILTER("createFilter"),
  UPDATE_FILTER("updateFilter"),
  DELETE_FILTER("deleteFilter"),
  ANALYZE_ITEM("analyzeItem"),
  CREATE_DEFECT("createDefect"),
  UPDATE_DEFECT("updateDefect"),
  DELETE_DEFECT("deleteDefect"),
  CREATE_INTEGRATION("createIntegration"),
  UPDATE_INTEGRATION("updateIntegration"),
  DELETE_INTEGRATION("deleteIntegration"),
  START_LAUNCH("startLaunch"),
  FINISH_LAUNCH("finishLaunch"),
  DELETE_LAUNCH("deleteLaunch"),
  MARK_LAUNCH_AS_IMPORTANT("markLaunchAsImportant"),
  UNMARK_LAUNCH_AS_IMPORTANT("unmarkLaunchAsImportant"),
  UPDATE_PROJECT("updateProject"),
  UPDATE_PROJECT_NAME("updateProjectName"),
  UPDATE_ANALYZER("updateAnalyzer"),
  POST_ISSUE("postIssue"),
  LINK_ISSUE("linkIssue"),
  LINK_ISSUE_AA("linkIssueAa"),
  UNLINK_ISSUE("unlinkIssue"),
  UPDATE_ITEM("updateItem"),
  CREATE_USER("createUser"),
  DELETE_INDEX("deleteIndex"),
  GENERATE_INDEX("generateIndex"),
  START_IMPORT("startImport"),
  FINISH_IMPORT("finishImport"),
  CREATE_PATTERN("createPattern"),
  UPDATE_PATTERN("updatePattern"),
  DELETE_PATTERN("deletePattern"),
  PATTERN_MATCHED("patternMatched"),
  ASSIGN_USER("assignUser"),
  UNASSIGN_USER("unassignUser"),
  DELETE_USER("deleteUser"),
  BULK_DELETE_USERS("bulkDeleteUsers"),
  CHANGE_ROLE("changeRole"),
  CREATE_PLUGIN("createPlugin"),
  DELETE_PLUGIN("deletePlugin"),
  UPDATE_PLUGIN("updatePlugin"),
  CREATE_PROJECT("createProject"),
  DELETE_PROJECT("deleteProject"),
  BULK_DELETE_PROJECT("bulkDeleteProject"),
  UPDATE_PATTERN_ANALYZER("updatePatternAnalysisSettings"),
  CREATE_ORGANIZATION("createOrganization"),
  UPDATE_ORGANIZATION("updateOrganization"),
  DELETE_ORGANIZATION("deleteOrganization"),
  CREATE_NOTIFICATION_RULE("createNotificationRule"),
  UPDATE_NOTIFICATION_RULE("updateNotificationRule"),
  DELETE_NOTIFICATION_RULE("deleteNotificationRule"),
  UPDATE_NOTIFICATION_SETTINGS("updateNotificationSettings"),

  UPDATE_ORGANIZATION_USERS("updateOrganizationUsers"),
  UPDATE_PROJECT_USERS("updateProjectUsers"),

  UPDATE_INSTANCE("updateInstance"),

  CREATE_TEST_CASE("createTestCase"),
  UPDATE_TEST_CASE("updateTestCase"),
  DELETE_TEST_CASE("deleteTestCase"),

  CREATE_LOG_TYPE("createLogType"),
  UPDATE_LOG_TYPE("updateLogType"),
  DELETE_LOG_TYPE("deleteLogType");

  private final String value;

  ActivityAction(String value) {
    this.value = value;
  }

  public static Optional<ActivityAction> fromString(String string) {
    return Optional.ofNullable(string).flatMap(
        str -> Arrays.stream(values()).filter(it -> it.value.equalsIgnoreCase(str)).findAny());
  }

  public String getValue() {
    return value;
  }
}
