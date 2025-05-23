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

package com.epam.ta.reportportal.exception.rest;

import com.epam.reportportal.rules.exception.ErrorType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;

/**
 * {@link ErrorType} to {@link HttpStatus} mapping.
 *
 * @author Andrei Varabyeu
 */
public class StatusCodeMapping {

  private StatusCodeMapping() {

  }

  private static final Map<ErrorType, HttpStatus> MAPPING = new HashMap<>() {
    private static final long serialVersionUID = 1L;

    {
      put(ErrorType.NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.LAUNCH_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.TEST_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.LOG_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.WIDGET_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.WIDGET_NOT_FOUND_IN_DASHBOARD, HttpStatus.NOT_FOUND);
      put(ErrorType.DASHBOARD_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.USER_FILTER_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.TEST_SUITE_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.ACTIVITY_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.ISSUE_TYPE_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.PROJECT_SETTINGS_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.TEST_ITEM_OR_LAUNCH_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.AUTH_INTEGRATION_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.USER_FILTER_NOT_FOUND_IN_PROJECT, HttpStatus.NOT_FOUND);
      put(ErrorType.WIDGET_NOT_FOUND_IN_PROJECT, HttpStatus.NOT_FOUND);
      put(ErrorType.DASHBOARD_NOT_FOUND_IN_PROJECT, HttpStatus.NOT_FOUND);
      put(ErrorType.PATTERN_TEMPLATE_NOT_FOUND_IN_PROJECT, HttpStatus.NOT_FOUND);
      put(ErrorType.ANALYZER_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.ATTACHMENT_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, HttpStatus.NOT_FOUND);
      put(ErrorType.CLUSTER_NOT_FOUND, HttpStatus.NOT_FOUND);

      put(ErrorType.RESOURCE_ALREADY_EXISTS, HttpStatus.CONFLICT);
      put(ErrorType.PROJECT_ALREADY_EXISTS, HttpStatus.CONFLICT);
      put(ErrorType.USER_ALREADY_EXISTS, HttpStatus.CONFLICT);
      put(ErrorType.USER_FILTER_ALREADY_EXISTS, HttpStatus.CONFLICT);
      put(ErrorType.ROLE_ALREADY_EXISTS_ERROR, HttpStatus.CONFLICT);

      put(ErrorType.FINISH_TIME_EARLIER_THAN_START_TIME, HttpStatus.NOT_ACCEPTABLE);
      put(ErrorType.CHILD_START_TIME_EARLIER_THAN_PARENT, HttpStatus.NOT_ACCEPTABLE);
      put(ErrorType.INCORRECT_FINISH_STATUS, HttpStatus.NOT_ACCEPTABLE);
      put(ErrorType.LAUNCH_IS_NOT_FINISHED, HttpStatus.NOT_ACCEPTABLE);
      put(ErrorType.TEST_ITEM_IS_NOT_FINISHED, HttpStatus.NOT_ACCEPTABLE);
      put(ErrorType.FINISH_LAUNCH_NOT_ALLOWED, HttpStatus.NOT_ACCEPTABLE);
      put(ErrorType.START_ITEM_NOT_ALLOWED, HttpStatus.NOT_ACCEPTABLE);
      put(ErrorType.FINISH_ITEM_NOT_ALLOWED, HttpStatus.NOT_ACCEPTABLE);
      put(ErrorType.LOGGING_IS_NOT_ALLOWED, HttpStatus.NOT_ACCEPTABLE);
      put(ErrorType.REPORTING_ITEM_ALREADY_FINISHED, HttpStatus.NOT_ACCEPTABLE);
      put(ErrorType.UNSUPPORTED_MERGE_STRATEGY_TYPE, HttpStatus.NOT_ACCEPTABLE);
      put(ErrorType.UNABLE_TO_CREATE_WIDGET, HttpStatus.NOT_ACCEPTABLE);
      put(ErrorType.UNABLE_LOAD_TEST_ITEM_HISTORY, HttpStatus.CONFLICT);
      put(ErrorType.DEMO_DATA_GENERATION_ERROR, HttpStatus.NOT_ACCEPTABLE);

      put(ErrorType.DASHBOARD_UPDATE_ERROR, HttpStatus.CONFLICT);
      put(ErrorType.BAD_SAVE_USER_FILTER_REQUEST, HttpStatus.BAD_REQUEST);
      put(ErrorType.BAD_SAVE_LOG_REQUEST, HttpStatus.BAD_REQUEST);
      put(ErrorType.UNSUPPORTED_TEST_ITEM_TYPE, HttpStatus.BAD_REQUEST);
      put(ErrorType.BAD_SAVE_WIDGET_REQUEST, HttpStatus.BAD_REQUEST);
      put(ErrorType.BAD_UPDATE_WIDGET_REQUEST, HttpStatus.BAD_REQUEST);
      put(ErrorType.FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION, HttpStatus.BAD_REQUEST);
      put(ErrorType.BAD_UPDATE_PREFERENCE_REQUEST, HttpStatus.BAD_REQUEST);
      put(ErrorType.IMPORT_FILE_ERROR, HttpStatus.BAD_REQUEST);
      put(ErrorType.PARSING_XML_ERROR, HttpStatus.BAD_REQUEST);
      put(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, HttpStatus.CONFLICT);
      put(ErrorType.PATTERN_ANALYSIS_ERROR, HttpStatus.BAD_REQUEST);

      put(ErrorType.INCORRECT_FILTER_PARAMETERS, HttpStatus.BAD_REQUEST);
      put(ErrorType.INCORRECT_SORTING_PARAMETERS, HttpStatus.BAD_REQUEST);
      put(ErrorType.PLUGIN_UPLOAD_ERROR, HttpStatus.BAD_REQUEST);
      put(ErrorType.PLUGIN_REMOVE_ERROR, HttpStatus.BAD_REQUEST);
      put(ErrorType.OBJECT_RETRIEVAL_ERROR, HttpStatus.BAD_REQUEST);
      put(ErrorType.UNABLE_TO_SAVE_CHILD_ITEM_FOR_THE_RETRY, HttpStatus.BAD_REQUEST);

      // ExternalSystem related
      put(ErrorType.INTEGRATION_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.INTEGRATION_ALREADY_EXISTS, HttpStatus.CONFLICT);
      put(ErrorType.PROJECT_NOT_CONFIGURED, HttpStatus.NOT_FOUND);
      put(ErrorType.INCORRECT_AUTHENTICATION_TYPE, HttpStatus.BAD_REQUEST);
      put(ErrorType.INCORRECT_INTEGRATION_NAME, HttpStatus.BAD_REQUEST);
      put(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, HttpStatus.CONFLICT);
      // ======================

      // Server Settings related
      put(ErrorType.SERVER_SETTINGS_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.SERVER_SETTINGS_ALREADY_EXISTS, HttpStatus.CONFLICT);
      put(ErrorType.EMAIL_CONFIGURATION_IS_INCORRECT, HttpStatus.FORBIDDEN);
      // ======================

      /* Authentication related */
      put(ErrorType.ACCESS_DENIED, HttpStatus.FORBIDDEN);
      put(ErrorType.ADDRESS_LOCKED, HttpStatus.FORBIDDEN);

      put(ErrorType.INCORRECT_REQUEST, HttpStatus.BAD_REQUEST);
      put(ErrorType.BAD_REQUEST_ERROR, HttpStatus.BAD_REQUEST);
      put(ErrorType.AMBIGUOUS_TEST_ITEM_STATUS, HttpStatus.BAD_REQUEST);
      put(ErrorType.UNCLASSIFIED_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
      put(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
      put(ErrorType.BINARY_DATA_CANNOT_BE_SAVED, HttpStatus.BAD_REQUEST);
      put(ErrorType.UNABLE_POST_TICKET, HttpStatus.BAD_REQUEST);
      put(ErrorType.FORBIDDEN_OPERATION, HttpStatus.BAD_REQUEST);
      put(ErrorType.UNABLE_ADD_TO_FAVORITE, HttpStatus.BAD_REQUEST);
      put(ErrorType.UNABLE_MODIFY_SHARABLE_RESOURCE, HttpStatus.UNPROCESSABLE_ENTITY);
      put(ErrorType.UNABLE_REMOVE_FROM_FAVORITE, HttpStatus.BAD_REQUEST);
      put(ErrorType.PROJECT_DOESNT_CONTAIN_USER, HttpStatus.UNPROCESSABLE_ENTITY);
      put(ErrorType.UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, HttpStatus.UNPROCESSABLE_ENTITY);
      put(ErrorType.UNABLE_TO_UPDATE_YOURSELF_ROLE, HttpStatus.UNPROCESSABLE_ENTITY);
      put(ErrorType.PROJECT_UPDATE_NOT_ALLOWED, HttpStatus.UNPROCESSABLE_ENTITY);
      put(ErrorType.TICKET_NOT_FOUND, HttpStatus.NOT_FOUND);
      put(ErrorType.ORGANIZATION_NOT_FOUND, HttpStatus.NOT_FOUND);

      put(ErrorType.RETRIES_HANDLER_ERROR, HttpStatus.BAD_REQUEST);

    }
  };

  /**
   * Returns the corresponding {@link HttpStatus} for the given {@link ErrorType}. If the error type is not mapped,
   * returns the provided default status.
   *
   * @param errorType     the error type to look up
   * @param defaultStatus the default HTTP status to return if no mapping is found
   * @return the mapped {@link HttpStatus} or the default status if not found
   */
  public static HttpStatus getHttpStatus(ErrorType errorType, HttpStatus defaultStatus) {
    return getHttpStatus(errorType).orElse(defaultStatus);
  }

  /**
   * Returns an {@link Optional} containing the {@link HttpStatus} mapped to the given {@link ErrorType}, or an empty
   * {@link Optional} if no mapping exists.
   *
   * @param errorType the error type to look up
   * @return an {@link Optional} with the mapped {@link HttpStatus}, or empty if not found
   */
  public static Optional<HttpStatus> getHttpStatus(ErrorType errorType) {
    return Optional.ofNullable(MAPPING.get(errorType));
  }

}
