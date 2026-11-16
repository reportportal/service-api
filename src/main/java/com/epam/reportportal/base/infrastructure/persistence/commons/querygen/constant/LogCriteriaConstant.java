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

package com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant;

/**
 * Search criteria fields for Log.
 *
 * @author Anton Machulski
 */
public final class LogCriteriaConstant {

  public static final String CRITERIA_TEST_ITEM_ID = "item";
  public static final String CRITERIA_LOG_LAUNCH_ID = "launch";
  public static final String CRITERIA_ITEM_LAUNCH_ID = "launchId";
  public static final String CRITERIA_LOG_MESSAGE = "message";
  public static final String CRITERIA_LOG_LEVEL = "level";
  public static final String CRITERIA_LOG_ID = "logId";
  public static final String CRITERIA_LOG_TIME = "logTime";
  public static final String CRITERIA_LOG_BINARY_CONTENT = "binaryContent";
  public static final String CRITERIA_LOG_PROJECT_ID = "projectId";

  private LogCriteriaConstant() {
    //static only
  }
}
