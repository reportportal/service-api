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

public final class TestItemCriteriaConstant {

  public static final String CRITERIA_STATUS = "status";
  public static final String CRITERIA_HAS_CHILDREN = "hasChildren";
  public static final String CRITERIA_HAS_RETRIES = "hasRetries";
  public static final String CRITERIA_HAS_STATS = "hasStats";
  public static final String CRITERIA_TYPE = "type";
  public static final String CRITERIA_PATH = "path";
  public static final String CRITERIA_ISSUE_TYPE = "issueType";
  public static final String CRITERIA_ISSUE_TYPE_ID = "issueTypeId";
  public static final String CRITERIA_ISSUE_GROUP_ID = "issueGroupId";
  public static final String CRITERIA_UNIQUE_ID = "uniqueId";
  public static final String CRITERIA_UUID = "uuid";
  public static final String CRITERIA_TEST_CASE_ID = "testCaseId";
  public static final String CRITERIA_TEST_CASE_HASH = "testCaseHash";
  public static final String CRITERIA_PARENT_ID = "parentId";
  public static final String CRITERIA_RETRY_PARENT_ID = "retryParentId";
  public static final String CRITERIA_RETRY_PARENT_LAUNCH_ID = "retryParentLaunchId";
  public static final String CRITERIA_DURATION = "duration";
  public static final String CRITERIA_PARAMETER_KEY = "key";
  public static final String CRITERIA_PARAMETER_VALUE = "value";
  public static final String CRITERIA_PATTERN_TEMPLATE_NAME = "patternName";
  public static final String CRITERIA_TICKET_ID = "ticketId";
  public static final String CRITERIA_CLUSTER_ID = "clusterId";
  public static final String RETRY_PARENT = "retry_parent";

  private TestItemCriteriaConstant() {
    //static only
  }

}
