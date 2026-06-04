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

package com.epam.reportportal.base.infrastructure.persistence.entity.enums;

import com.epam.reportportal.base.infrastructure.persistence.entity.AnalyzeMode;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectAnalyzerConfig;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

/**
 * Enum with a list of basic required project parameters
 *
 * @author Andrey Plisunov
 */
public enum ProjectAttributeEnum {

  NOTIFICATIONS_ENABLED(Prefix.NOTIFICATIONS + "enabled", String.valueOf(true)),
  NOTIFICATIONS_EMAIL_ENABLED(Prefix.NOTIFICATIONS + "email.enabled", String.valueOf(true)),
  NOTIFICATIONS_TELEGRAM_ENABLED(Prefix.NOTIFICATIONS + "telegram.enabled", String.valueOf(true)),
  NOTIFICATIONS_SLACK_ENABLED(Prefix.NOTIFICATIONS + "slack.enabled", String.valueOf(true)),

  INTERRUPT_JOB_TIME(Prefix.JOB + "interruptJobTime",
      String.valueOf(Duration.ofDays(1).toSeconds())),
  KEEP_LAUNCHES(Prefix.JOB + "keepLaunches", String.valueOf(Duration.ofDays(90).toSeconds())),
  KEEP_LOGS(Prefix.JOB + "keepLogs", String.valueOf(Duration.ofDays(90).toSeconds())),
  KEEP_SCREENSHOTS(Prefix.JOB + "keepScreenshots", String.valueOf(Duration.ofDays(14).toSeconds())),

  MIN_SHOULD_MATCH(Prefix.ANALYZER + "minShouldMatch",
      String.valueOf(ProjectAnalyzerConfig.MIN_SHOULD_MATCH)),
  NUMBER_OF_LOG_LINES(Prefix.ANALYZER + "numberOfLogLines",
      String.valueOf(ProjectAnalyzerConfig.NUMBER_OF_LOG_LINES)),
  INDEXING_RUNNING(Prefix.ANALYZER + "indexingRunning", String.valueOf(false)),
  AUTO_PATTERN_ANALYZER_ENABLED(Prefix.ANALYZER + "isAutoPatternAnalyzerEnabled",
      String.valueOf(false)),
  AUTO_ANALYZER_ENABLED(Prefix.ANALYZER + "isAutoAnalyzerEnabled", String.valueOf(true)),
  AUTO_ANALYZER_MODE(Prefix.ANALYZER + "autoAnalyzerMode", AnalyzeMode.CURRENT_AND_THE_SAME_NAME.getValue()),
  ALL_MESSAGES_SHOULD_MATCH(Prefix.ANALYZER + "allMessagesShouldMatch", String.valueOf(false)),
  SEARCH_LOGS_MIN_SHOULD_MATCH(Prefix.ANALYZER + "searchLogsMinShouldMatch",
      String.valueOf(ProjectAnalyzerConfig.MIN_SHOULD_MATCH)),
  AUTO_UNIQUE_ERROR_ANALYZER_ENABLED(Prefix.ANALYZER + Prefix.UNIQUE_ERROR + "enabled",
      String.valueOf(true)),
  UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS(Prefix.ANALYZER + Prefix.UNIQUE_ERROR + "removeNumbers",
      String.valueOf(true)),
  LARGEST_RETRY_PRIORITY(Prefix.ANALYZER + "largestRetryPriority", String.valueOf(false));

  public static final String FOREVER_ALIAS = "0";
  private String attribute;
  private String defaultValue;

  ProjectAttributeEnum(String attribute, String defaultValue) {
    this.attribute = attribute;
    this.defaultValue = defaultValue;
  }

  public static Optional<ProjectAttributeEnum> findByAttributeName(String attributeName) {
    return Arrays.stream(ProjectAttributeEnum.values())
        .filter(v -> v.getAttribute().equalsIgnoreCase(attributeName)).findAny();
  }

  public static boolean isPresent(String name) {
    return findByAttributeName(name).isPresent();
  }

  public String getAttribute() {
    return attribute;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public static class Prefix {

    public static final String ANALYZER = "analyzer.";
    public static final String JOB = "job.";
    public static final String UNIQUE_ERROR = "uniqueError.";
    public static final String NOTIFICATIONS = "notifications.";
  }
}


