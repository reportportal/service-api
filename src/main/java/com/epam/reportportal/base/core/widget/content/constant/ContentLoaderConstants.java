/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.widget.content.constant;

/**
 * Constants for widget content-loader parameter keys and field names.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class ContentLoaderConstants {

  public static final String CONTENT_FIELDS_DELIMITER = ",";

  public static final String RESULT = "result";
  public static final String LATEST_OPTION = "latest";
  public static final String LATEST_LAUNCH = "latestLaunch";
  public static final String LAUNCH_NAME_FIELD = "launchNameFilter";
  public static final String USER = "user";
  public static final String ACTION_TYPE = "actionType";
  public static final String ATTRIBUTES = "attributes";
  public static final String ATTRIBUTE_KEY = "attributeKey";
  public static final String PATTERN_TEMPLATE_NAME = "patternTemplateName";
  public static final String ITEM_TYPE = "type";
  public static final String INCLUDE_METHODS = "includeMethods";
  public static final String FLAKY = "flaky";
  /** CCAAS test stability (flakiness) table payload key */
  public static final String TEST_STABILITY_FLAKINESS = "testStabilityFlakiness";
  public static final String TEST_STABILITY_PER_LAUNCH = "testStabilityPerLaunch";
  public static final String LAUNCH_BREAKDOWN = "launchBreakdown";
  public static final String BREAKDOWN_TEST_NAME = "breakdownTestName";
  public static final String CUSTOM_COLUMNS = "customColumns";
  public static final String TIMELINE = "timeline";
  public static final String ATTRIBUTE_KEYS = "attributeKeys";
  /** When true, merge rows that share the same test {@code name} but different RP {@code unique_id}. */
  public static final String AGGREGATE_BY_TEST_NAME = "aggregateByTestName";
  /**
   * When true (default if omitted), include only the latest {@code launch.id} per {@code launch.name}
   * — aligns widget totals with the Launches page filter view.
   */
  public static final String LATEST_LAUNCHES_ONLY = "latestLaunchesOnly";
  public static final String MIN_PASSING_RATE = "minPassingRate";
  public static final String EXCLUDE_SKIPPED = "excludeSkipped";


  private ContentLoaderConstants() {
    //static only
  }
}
