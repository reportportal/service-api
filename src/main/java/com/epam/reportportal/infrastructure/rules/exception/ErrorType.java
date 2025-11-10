/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.infrastructure.rules.exception;

import lombok.Getter;

/**
 * Report Portal's exception list
 *
 * @author Andrei Varabyeu
 */
@Getter
public enum ErrorType {

  /**
   * Incorrect Report Portal WS Request
   */
  INCORRECT_REQUEST(4001, "Incorrect Request. {}"),

  /**
   * Incorrect Report Portal WS Request
   */
  BINARY_DATA_CANNOT_BE_SAVED(4002, "Binary data cannot be saved. {}"),

  /**
   * Access Denied
   */
  ACCESS_DENIED(4003, "You do not have enough permissions. {}"),

  /**
   * Access Denied
   */
  ADDRESS_LOCKED(4004, "Address is locked due to several incorrect login attempts"),

  /**
   * UPSA users denied
   */
  UPSA_USER_DENIED(4005,
      "Unable to assign/unassign user to/from project. Please verify user assignment to the organization in EPAM internal system: delivery.epam.com"),

  /**
   * If operation requires payment
   */
  PAYMENT_REQUIRED(4020, "Payment Required. {}"),

  /**
   * If operation requires a paid plugin
   */
  PAID_PLUGIN_REQUIRED(4021, "Plugin '{}' is required. {}"),

  /**
   * If specified by id Project or by ProjectName not found
   */
  PROJECT_NOT_FOUND(4040, "Project '{}' not found. Did you use correct project name?"),

  /**
   * If specified by id Launch not found
   */
  LAUNCH_NOT_FOUND(4041, "Launch '{}' not found. Did you use correct Launch ID?"),

  /**
   * If specified by id TestSuite not found
   */
  TEST_SUITE_NOT_FOUND(4042, "TestSuite Not Found. Did you use correct TestSuite ID?"),

  /**
   * If specified by id Test not found
   */
  TEST_ITEM_NOT_FOUND(4043, "Test Item '{}' not found. Did you use correct Test Item ID?"),

  /**
   * If specified by id Log not found
   */
  LOG_NOT_FOUND(4044, "Log '{}' not found. Did you use correct Log ID?"),

  /**
   * If specified by id role not found
   */
  ROLE_NOT_FOUND(4045, "Project role '{}' not found. Did you use correct Role Name?"),

  /**
   * If specified by login User not found
   */
  USER_NOT_FOUND(4046, "User '{}' not found. {}"),

  /**
   * If specified by id Widget not found
   */
  WIDGET_NOT_FOUND(4047, "Widget with ID '{}' not found. Did you use correct Widget ID?"),

  /**
   * If specified by id Widget not found
   */
  WIDGET_NOT_FOUND_IN_DASHBOARD(4048,
      "Widget with ID '{}' not found in dashboard '{}'. Did you use correct Widget ID?"),

  /**
   * If specified by id Dashboard not found
   */
  DASHBOARD_NOT_FOUND(4049, "Dashboard with ID '{}' not found. Did you use correct Dashboard ID?"),

  /**
   * If specified by id UserFilter not found
   */
  USER_FILTER_NOT_FOUND(40410,
      "User filter with ID '{}' is not found on project '{}' for user '{}'. Did you use correct User Filter ID?"
  ),

  /**
   * If specified by id Activity not found
   */
  ACTIVITY_NOT_FOUND(40411, "Activity '{}' not found. Did you use correct Activity ID?"),

  /**
   * Unable to create widget based on favorite link
   */
  UNABLE_TO_CREATE_WIDGET(40412, "Unable to create or update widget. {}"),

  /**
   * Integration not found
   */
  INTEGRATION_NOT_FOUND(40413, "Integration with ID '{}' not found. Did you use correct ID?"),

  /**
   * If project not configured
   */
  PROJECT_NOT_CONFIGURED(40414, "Project '{}' not configured."),

  /**
   * If server settings for specified profile not found
   */
  SERVER_SETTINGS_NOT_FOUND(40415, "Server Settings with '{}' profile not found."),

  /**
   * If issue type not found (including custom project specific sub-types)
   */
  ISSUE_TYPE_NOT_FOUND(40416, "Issue Type '{}' not found."),

  /**
   * If project settings for specified project not found
   */
  PROJECT_SETTINGS_NOT_FOUND(40417, "Project Settings for project '{}' not found."),

  /**
   * Ticket not found
   */
  TICKET_NOT_FOUND(40418, "Ticket with ID '{}' not found. Did you use correct Ticket ID?"),

  /**
   * If specified Authentication extension isn't found
   */
  AUTH_INTEGRATION_NOT_FOUND(40419, "Auth integration '{}' not found. Did you use correct name?"),

  /**
   * If specified by id Widget not found
   */
  WIDGET_NOT_FOUND_IN_PROJECT(40420,
      "Widget with ID '{}' not found on project '{}'. Did you use correct Widget ID?"),

  /**
   * If specified by id UserFilter not found
   */
  USER_FILTER_NOT_FOUND_IN_PROJECT(40421,
      "User filter with ID '{}' not found on project '{}'. Did you use correct User Filter ID?"),

  /**
   * If specified by id Dashboard not found
   */
  DASHBOARD_NOT_FOUND_IN_PROJECT(40422,
      "Dashboard with ID '{}' not found on project '{}'. Did you use correct Dashboard ID?"),

  /**
   * If pattern template with provided id is not found
   */
  PATTERN_TEMPLATE_NOT_FOUND_IN_PROJECT(40423,
      "Pattern template with ID '{}' not found on project '{}'. Did you use correct Pattern template ID?"
  ),

  TEST_ITEM_OR_LAUNCH_NOT_FOUND(40424,
      "Test Item or Launch '{}' not found. Did you use correct ID?"),

  /**
   * If analyzer with provided name is not found
   */
  ANALYZER_NOT_FOUND(40425, "Analyzer '{}' not found."),

  /**
   * If attachment with provided id not found
   */
  ATTACHMENT_NOT_FOUND(40426, "Attachment '{}' not found"),

  /**
   * If binary data not found
   */
  UNABLE_TO_LOAD_BINARY_DATA(40427, "Unable to load binary data by id '{}'"),

  /**
   * If cluster with provided id not found
   */
  CLUSTER_NOT_FOUND(40428, "Cluster '{}' not found"),

  /**
   * If specified by id Organization not found
   */
  ORGANIZATION_NOT_FOUND(40429,
      "Organization '{}' not found. Did you use correct Organization ID?"),

  /**
   * /** Common error in case if object not found
   */
  NOT_FOUND(40430, "'{}' not found. Did you use correct ID?"),

  /**
   * If provided filtering parameters are incorrect
   */
  INCORRECT_FILTER_PARAMETERS(40011, "Incorrect filtering parameters. {}"),

  /**
   * If specified by id Log not found
   */
  INCORRECT_SORTING_PARAMETERS(40012, "Sorting parameter {} is not defined"),

  /**
   * If it's impossible to use specified integration
   */
  INCORRECT_INTEGRATION_NAME(40013, "Incorrect integration name. {}"),

  /**
   * Unable modify sharable resource
   */
  UNABLE_MODIFY_SHARABLE_RESOURCE(40014, "Unable modify sharable resource. {}"),

  /**
   * Unable to recognize provided authentication type
   */
  INCORRECT_AUTHENTICATION_TYPE(40015, "Incorrect authentication type: {}"),

  /**
   * Impossible post ticket to BTS
   */
  UNABLE_POST_TICKET(40301, "Impossible post ticket. {}"),

  /**
   * Impossible to interact with integration
   */
  UNABLE_INTERACT_WITH_INTEGRATION(40302, "Impossible interact with integration. {}"),

  /**
   * "Unable to assign/unassign user to/from project
   */
  UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT(40304, "Unable to assign/unassign user to/from project. {}"),

  /**
   * Impossible operation on server side
   */
  EMAIL_CONFIGURATION_IS_INCORRECT(40305,
      "Email server is not configured or configuration is incorrect. {}"),

  /**
   * Project update not allowed. This restriction is applied to Personal projects and internal EPAM's project type
   */
  PROJECT_UPDATE_NOT_ALLOWED(4007, "Update/Delete of project with type {} is not allowed"),

  /**
   * Unable to update yourself role
   */
  UNABLE_TO_UPDATE_YOURSELF_ROLE(4008, "Unable to update yourself role."),

  /**
   * Impossible operation on server side
   */
  FORBIDDEN_OPERATION(40010, "Forbidden operation. {}"),

  /**
   * If resource with specified settings already exists and should be unique
   */
  RESOURCE_ALREADY_EXISTS(4091, "Resource '{}' already exists. You couldn't create the duplicate."),

  /**
   * If Role with specified settings already exists and should be unique
   */
  ROLE_ALREADY_EXISTS_ERROR(4093,
      "Role with specified settings already exists. You couldn't create the duplicate."),

  /**
   * If User with specified login already exists and should be unique
   */
  USER_ALREADY_EXISTS(4094, "User with '{}' already exists. You couldn't create the duplicate."),

  /**
   * If User filter with specified name already exists and should be unique
   */
  USER_FILTER_ALREADY_EXISTS(4098,
      "User filter with name '{}' already exists for user '{}' under the project '{}'. You couldn't create the duplicate."
  ),

  /**
   * If Project with specified settings already exists and should be unique
   */
  PROJECT_ALREADY_EXISTS(4095, "Project '{}' already exists. You couldn't create the duplicate."),

  /**
   * If Dashboard update request contains invalid data
   */
  DASHBOARD_UPDATE_ERROR(4096, "Dashboard update request contains invalid data. {}"),

  /**
   * If widget content can't be loaded because some of the properties are incorrect
   */
  UNABLE_LOAD_WIDGET_CONTENT(4097,
      "Unable to load widget content. Widget properties contain errors: {}"),

  /**
   * Unable add resource to favorites
   */
  UNABLE_ADD_TO_FAVORITE(4099, "Unable add resource to favorites. {}"),

  /**
   * Unable create duplicate of integration
   */
  INTEGRATION_ALREADY_EXISTS(40910,
      "Integration '{}' already exists. You couldn't create the duplicate."),

  /**
   * Unable create the duplication of server settings with one profile name
   */
  SERVER_SETTINGS_ALREADY_EXISTS(40911,
      "Server settings with '{}' profile already exists. You couldn't create the duplicate."),

  /**
   * Unable to assign user to the organization or project twice
   */
  USER_ALREADY_ASSIGNED(40912, "User '{}' cannot be assigned to {} twice. You couldn't create the duplicate."),

  /**
   * Unable remove resource from favorites
   */
  UNABLE_REMOVE_FROM_FAVORITE(4100, "Unable remove resource from favorites. {}"),

  /**
   * If specified by login User not found
   */
  LAUNCH_IS_NOT_FINISHED(4063, "Unable to perform operation for non-finished launch. {}"),

  /**
   * Unable to finish with incorrect status
   */
  TEST_ITEM_IS_NOT_FINISHED(4064, "Unable to perform operation for non-finished test item. {}"),

  /**
   * Unable to finish with incorrect status
   */
  INCORRECT_FINISH_STATUS(4065, "{}. Did you provide correct status in request?"),

  /**
   * Base Error If Request sent with incorrect parameters
   */
  BAD_REQUEST_ERROR(40016, "Error in handled Request. Please, check specified parameters: '{}'"),
  /**
   * If SaveLogRQ sent with incorrect parameters
   */
  BAD_SAVE_LOG_REQUEST(40017, "Error in Save Log Request. {}"),
  /**
   * If Test, TestStep, Launch already finished and writing is disabled
   */
  REPORTING_ITEM_ALREADY_FINISHED(40018,
      "Reporting for item {} already finished. Please, check item status."),

  /**
   * Occurs when status is not provided and cannot be calculated or provided status does not corresponds to calculated
   * from statistics.
   */
  AMBIGUOUS_TEST_ITEM_STATUS(40019, "Test item status is ambiguous. {}"),

  /**
   * Incorrect test item description
   */
  FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION(40020, "Test items issue type cannot be resolved. {}"),

  /**
   * Finish Time Earlier than start time
   */
  FINISH_TIME_EARLIER_THAN_START_TIME(40021,
      "Finish time '{}' is earlier than start time '{}' for resource with ID '{}'"),

  /**
   * Is not allowed to finish item
   */
  FINISH_ITEM_NOT_ALLOWED(40022, "Finish test item is not allowed. {}"),

  /**
   * Unable to finish in current status
   */
  FINISH_LAUNCH_NOT_ALLOWED(40023, "Finish launch is not allowed. {}"),

  /**
   * Unable to finish in current status
   */
  START_ITEM_NOT_ALLOWED(40024, "Start test item is not allowed. {}"),

  /**
   * Finish Time Earlier than start time
   */
  CHILD_START_TIME_EARLIER_THAN_PARENT(40025,
      "Start time of child ['{}'] item should be same or later than start time ['{}'] of the parent item/launch '{}'"
  ),

  /**
   * Unsupported test item type
   */
  UNSUPPORTED_TEST_ITEM_TYPE(40026, "Test Item type {} is unsupported"),

  /**
   * Unsupported test item type
   */
  LOGGING_IS_NOT_ALLOWED(40027, "Logging is not allowed. {}"),

  /**
   * Incorrect create widget request
   */
  BAD_SAVE_WIDGET_REQUEST(40028, "Incorrect create widget request. {}"),

  /**
   * Incorrect update widget request
   */
  BAD_UPDATE_WIDGET_REQUEST(40029, "Incorrect update widget request. {}"),

  /**
   * Unable to load history test item's history.
   */
  UNABLE_LOAD_TEST_ITEM_HISTORY(40030, "Unable to load test item history. {}"),

  /**
   * Bad save user filter request
   */
  BAD_SAVE_USER_FILTER_REQUEST(40031, "Bad save user filter request. {}"),

  /**
   * Error while processing retries
   */
  RETRIES_HANDLER_ERROR(40036, "Incorrect retries processing. {}"),

  /**
   * Bad format of importing file
   */
  IMPORT_FILE_ERROR(40035, "Error while importing the file. '{}'"),

  /**
   * Error during the xml file parsing
   */
  PARSING_XML_ERROR(40037, "Error during parsing the xml file: '{}'"),

  /**
   * Error during the object retrieving
   */
  OBJECT_RETRIEVAL_ERROR(40038, "Error during object retrieving: '{}'"),

  /**
   * Error during the plugin uploading
   */
  PLUGIN_UPLOAD_ERROR(40039, "Error during plugin uploading: '{}'"),

  /**
   * Error during the plugin removing
   */
  PLUGIN_REMOVE_ERROR(40040, "Error during plugin removing: '{}'"),

  /**
   * Unable to save child item for a retry
   */
  UNABLE_TO_SAVE_CHILD_ITEM_FOR_THE_RETRY(40041,
      "Item with id = '{}' is a retry and can not have children"),

  /**
   * Pattern analysis error
   */
  PATTERN_ANALYSIS_ERROR(40042, "Pattern analysis error. {}"),

  /**
   * Bad save user filter request
   */
  PROJECT_DOESNT_CONTAIN_USER(4220, "Project '{}' doesn't contain user '{}'"),

  /**
   * Base ReportPortal Exception. Try to avoid this type and create more custom
   */
  UNCLASSIFIED_REPORT_PORTAL_ERROR(5001, "Unclassified Report Portal Error"),

  /**
   * Incorrect update preference request
   */
  BAD_UPDATE_PREFERENCE_REQUEST(40032, "Incorrect update widget request {}"),

  /**
   * Unsupported merge strategy type
   */
  UNSUPPORTED_MERGE_STRATEGY_TYPE(40033, "Merge Strategy type {} is unsupported"),

  /**
   * Unable to recognize provided authentication type
   */
  DEMO_DATA_GENERATION_ERROR(40034, "Demo Data Generation error: {}"),

  /**
   * Use it If there are no any other exceptions. There should by no such exception
   */
  UNCLASSIFIED_ERROR(5000, "Unclassified error");

  private final int code;

  private final String description;

  ErrorType(int code, String description) {
    this.code = code;
    this.description = description;
  }

  /**
   * Get instance by code
   *
   * @param code Error Code
   * @return ErrorType
   */
  public static ErrorType getByCode(int code) {
    for (ErrorType error : values()) {
      if (error.getCode() == code) {
        return error;
      }
    }
    throw new IllegalArgumentException("Unable to find Error with code '" + code + "'");
  }
}
