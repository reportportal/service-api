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
import lombok.Getter;

/**
 * String codes for user-visible actions (dashboard, widget, user, BTS, etc.).
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Getter
public enum ActivityAction {

  CREATE_DASHBOARD("createDashboard"),
  UPDATE_DASHBOARD("updateDashboard"),
  UPDATE_DASHBOARD_STATE("updateDashboardState"),
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

  //TMS
  CREATE_TEST_CASE("Created Test Case"),
  DELETE_TEST_CASE("Deleted Test Case"),
  // --- Name ---
  UPDATE_TEST_CASE_NAME("Updated name"),
  // --- Description ---
  CREATE_TEST_CASE_DESCRIPTION("Added description"),
  UPDATE_TEST_CASE_DESCRIPTION("Updated description"),
  DELETE_TEST_CASE_DESCRIPTION("Deleted description"),
  // --- Priority ---
  CREATE_TEST_CASE_PRIORITY("Added priority"),
  UPDATE_TEST_CASE_PRIORITY("Updated priority"),
  DELETE_TEST_CASE_PRIORITY("Deleted priority"),
  // --- Tags (attributes) ---
  CREATE_TEST_CASE_TAGS("Added tags"),
  UPDATE_TEST_CASE_TAGS("Updated tags"),
  DELETE_TEST_CASE_TAGS("Deleted tags"),
  // --- External ID ---
  CREATE_TEST_CASE_EXTERNAL_ID("Created external ID"),
  UPDATE_TEST_CASE_EXTERNAL_ID("Updated external ID"),
  DELETE_TEST_CASE_EXTERNAL_ID("Deleted external ID"),
  // --- Test Folder (moving of the test case) ---
  UPDATE_TEST_CASE_TEST_FOLDER_ID("Updated test folder"),
  // --- Execution Estimation Time ---
  CREATE_TEST_CASE_EXECUTION_ESTIMATION_TIME("Added execution estimation time"),
  UPDATE_TEST_CASE_EXECUTION_ESTIMATION_TIME("Updated execution estimation time"),
  DELETE_TEST_CASE_EXECUTION_ESTIMATION_TIME("Deleted execution estimation time"),
  // --- Type ---
  CREATE_TEST_CASE_TYPE("Added type"),
  UPDATE_TEST_CASE_TYPE("Updated type"),
  DELETE_TEST_CASE_TYPE("Deleted type"),
  // --- Instructions (Manual Scenario) ---
  CREATE_TEST_CASE_INSTRUCTIONS("Added instructions"),
  UPDATE_TEST_CASE_INSTRUCTIONS("Updated instructions"),
  DELETE_TEST_CASE_INSTRUCTIONS("Deleted instructions"),
  // --- Expected Result (Manual Scenario) ---
  CREATE_TEST_CASE_EXPECTED_RESULT("Added expected result"),
  UPDATE_TEST_CASE_EXPECTED_RESULT("Updated expected result"),
  DELETE_TEST_CASE_EXPECTED_RESULT("Deleted expected result"),
  // --- Preconditions ---
  CREATE_TEST_CASE_PRECONDITIONS("Added preconditions"),
  UPDATE_TEST_CASE_PRECONDITIONS("Updated preconditions"),
  DELETE_TEST_CASE_PRECONDITIONS("Deleted preconditions"),
  // --- Preconditions Attachments ---
  CREATE_TEST_CASE_PRECONDITIONS_ATTACHMENTS("Added preconditions attachments"),
  UPDATE_TEST_CASE_PRECONDITIONS_ATTACHMENTS("Updated preconditions attachments"),
  DELETE_TEST_CASE_PRECONDITIONS_ATTACHMENTS("Deleted preconditions attachments"),
  // --- Steps ---
  CREATE_TEST_CASE_STEPS("Added steps"),
  UPDATE_TEST_CASE_STEPS("Updated steps"),
  DELETE_TEST_CASE_STEPS("Deleted steps"),
  // --- Requirements ---
  CREATE_TEST_CASE_REQUIREMENTS("Added requirements"),
  UPDATE_TEST_CASE_REQUIREMENTS("Updated requirements"),
  DELETE_TEST_CASE_REQUIREMENTS("Deleted requirements"),
  // --- Attachments (Manual Scenario) ---
  CREATE_TEST_CASE_MANUAL_SCENARIO_ATTACHMENTS("Added manual scenario attachments"),
  UPDATE_TEST_CASE_MANUAL_SCENARIO_ATTACHMENTS("Updated manual scenario attachments"),
  DELETE_TEST_CASE_MANUAL_SCENARIO_ATTACHMENTS("Deleted manual scenario attachments"),
  // --- Attachments (Manual Scenario Step) ---
  CREATE_TEST_CASE_MANUAL_SCENARIO_STEP_ATTACHMENTS("Added manual scenario step attachments"),
  UPDATE_TEST_CASE_MANUAL_SCENARIO_STEP_ATTACHMENTS("Updated manual scenario step attachments"),
  DELETE_TEST_CASE_MANUAL_SCENARIO_STEP_ATTACHMENTS("Deleted manual scenario step attachments"),

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

}
