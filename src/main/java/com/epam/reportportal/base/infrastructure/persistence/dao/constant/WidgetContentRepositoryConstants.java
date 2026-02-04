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

package com.epam.reportportal.base.infrastructure.persistence.dao.constant;

/**
 * @author Ivan Budaev
 */
public class WidgetContentRepositoryConstants {

  public static final String EXECUTIONS_TOTAL = "statistics$executions$total";
  public static final String EXECUTIONS_FAILED = "statistics$executions$failed";
  public static final String EXECUTIONS_SKIPPED = "statistics$executions$skipped";
  public static final String EXECUTIONS_PASSED = "statistics$executions$passed";
  public static final String EXECUTIONS_STOPPED = "statistics$executions$stopped";

  public static final String DEFECTS_AUTOMATION_BUG_TOTAL = "statistics$defects$automation_bug$total";
  public static final String DEFECTS_PRODUCT_BUG_TOTAL = "statistics$defects$product_bug$total";
  public static final String DEFECTS_NO_DEFECT_TOTAL = "statistics$defects$no_defect$total";
  public static final String DEFECTS_SYSTEM_ISSUE_TOTAL = "statistics$defects$system_issue$total";
  public static final String DEFECTS_TO_INVESTIGATE_TOTAL = "statistics$defects$to_investigate$total";

  public static final String STATISTICS_SEPARATOR = "$";

  public static final String TOTAL = "total";
  public static final String SKIPPED = "skipped";
  public static final String EXECUTIONS_KEY = "executions";
  public static final String DEFECTS_KEY = "defects";

  public static final String STATISTICS_TABLE = "statistics_table";
  public static final String STATISTICS_COUNTER = "s_counter";
  public static final String SF_NAME = "sf_name";

  /*Constants for result query mapping*/
  public static final Double PERCENTAGE_MULTIPLIER = 100d;
  public static final Integer ZERO_QUERY_VALUE = 0;
  public static final String LAUNCH_ID = "launch_id";
  public static final String NAME = "name";
  public static final String DESCRIPTION = "description";
  public static final String TARGET = "target";
  public static final String ID = "id";
  public static final String STATUS = "status";
  public static final String NUMBER = "number";
  public static final String END_TIME = "endTime";
  public static final String USER_ID = "userId";
  public static final String ORGANIZATION_ID = "organizationId";
  public static final String PROJECT_ID = "projectId";

  /* Most failed widget constants */
  public static final String HISTORY = "history";
  public static final String CRITERIA = "criteria";
  public static final String STATUS_HISTORY = "statusHistory";
  public static final String START_TIME_HISTORY = "startTimeHistory";
  public static final String CRITERIA_TABLE = "criteria_table";
  public static final String CRITERIA_FLAG = "criteria_flag";
  public static final String ITEM_ID = "item_id";

  public static final Integer MOST_FAILED_CRITERIA_LIMIT = 50;

  /* Overall statistics widget constants */
  public static final String LAUNCHES = "launches";

  /*Flaky test table widget constants*/
  public static final String FLAKY_TABLE_RESULTS = "flaky";
  public static final Integer FLAKY_CASES_LIMIT = 50;

  /*Activity table widget constants*/
  public static final String ACTIVITIES = "activities";

  /*Investigation widget constants*/
  public static final String INVESTIGATED = "investigated";
  public static final String TO_INVESTIGATE = "toInvestigate";

  /*Launch pass widget constants*/
  public static final String PASSED = "passed";

  /*Cases trend widget constants*/
  public static final String DELTA = "delta";

  /*Launches duration widget constants*/
  public static final String ITEMS = "items";
  public static final String DURATION = "duration";

  /*Not passed cases widget constants*/
  public static final String PERCENTAGE = "percentage";
  public static final String NOT_PASSED_STATISTICS_KEY = "% (Failed+Skipped)/Total";

  /*Unique bugs table widget constants*/
  public static final String ITEM_ATTRIBUTES = "item_attributes";
  public static final String KEY = "key";
  public static final String VALUE = "value";

  /*Flaky cases table widget constants*/
  public static final String UNIQUE_ID = "unique_id";
  public static final String ITEM_NAME = "item_name";
  public static final String STATUSES = "statuses";
  public static final String SWITCH_FLAG = "switchFlag";
  public static final String FLAKY_COUNT = "flakyCount";

  /*Cumulative trend widget constants*/
  public static final String LAUNCHES_TABLE = "launches_table";
  public static final String AGGREGATED_LAUNCHES_IDS = "aggregated_launches_ids";
  public static final String START_TIME = "start_time";
  public static final String FIRST_LEVEL_ID = "first_level_id";
  public static final String LATEST_NUMBER = "latest_number";
  public static final String VERSION_PATTERN = "^(\\d+)(\\.\\d+)*$";
  public static final String VERSION_DELIMITER = ".";

  /*Product status widget constants*/
  public static final String ATTRIBUTE_VALUE = "attribute_value";
  public static final String ATTRIBUTE_KEY = "attribute_key";
  public static final String FILTER_NAME = "filter_name";
  public static final String ATTRIBUTE_VALUES = "attribute_values";
  public static final String PASSING_RATE = "passingRate";
  public static final String SUM = "sum";
  public static final String AVERAGE_PASSING_RATE = "averagePassingRate";
  public static final String ATTR_ID = "attr_id";
  public static final String ATTR_TABLE = "attr_table";

  /*Top pattern templates widget constants*/
  public static final Integer PATTERNS_COUNT = 20;

  /*Health check table widget constants*/
  public static final String CUSTOM_ATTRIBUTE = "custom_attribute";
  public static final String AGGREGATED_VALUES = "aggregated_values";
  public static final String CUSTOM_COLUMN = "custom_column";
  public static final String CUSTOM_COLUMN_SORTING = "customColumn";
  public static final String EXCLUDE_SKIPPED_TABLE = "exclude_skipped_table";

}
