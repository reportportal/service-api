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

package com.epam.reportportal.ws.rabbit.activity.util;

import com.epam.reportportal.infrastructure.persistence.entity.activity.HistoryField;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utilities for building {@link com.epam.reportportal.infrastructure.persistence.entity.activity.HistoryField}
 * instances describing changes of entity attributes for activity logging.
 *
 * <p>Provides helpers for string, boolean and parameter comparisons, as well as configuration
 * extraction and comparison utilities used across activity event creation.</p>
 *
 * @author Ihar Kahadouski
 */
public class ActivityDetailsUtil {

  private ActivityDetailsUtil() {
    //static only
  }

  public static final String NAME = "name";

  public static final String VALUE = "value";
  public static final String DESCRIPTION = "description";
  public static final String EMPTY_FIELD = "";
  public static final String TICKET_ID = "ticketId";
  public static final String LAUNCH_INACTIVITY = "launchInactivity";
  public static final String EMPTY_STRING = "";
  public static final String COMMENT = "comment";
  public static final String ISSUE_TYPE = "issueType";
  public static final String IGNORE_ANALYZER = "ignoreAnalyzer";
  public static final String EMAIL_STATUS = "emailEnabled";
  public static final String EMAIL_CASES = "emailCases";
  public static final String EMAIL_FROM = "from";
  public static final String ITEMS_COUNT = "itemsCount";
  public static final String CONTENT_FIELDS = "contentFields";
  public static final String WIDGET_OPTIONS = "widgetOptions";
  public static final String STATUS = "status";
  public static final String RELEVANT_ITEM = "relevantItem";
  public static final String ENABLED = "enabled";
  public static final String ITEM_IDS = "itemIds";
  public static final String LAUNCH_ID = "launchId";
  public static final String PATTERN_NAME = "patternName";
  public static final String RP_SUBJECT_NAME = "ReportPortal";

  public static final String RECIPIENTS = "recipients";
  public static final String LAUNCH_NAMES = "launchNames";
  public static final String ATTRIBUTES = "attributes";
  public static final String SEND_CASE = "sendCase";
  public static final String TYPE = "type";
  public static final String ATTRIBUTES_OPERATOR = "attributesOperator";
  public static final String RULE_DETAILS = "ruleDetails";

  public static Optional<HistoryField> processList(String fieldName, List<String> before, List<String> after) {
    var left = normalizeList(before);
    var right = normalizeList(after);
    return left.equals(right)
        ? Optional.empty()
        : Optional.of(HistoryField.of(fieldName, String.join(", ", left), String.join(", ", right)));
  }

  public static Optional<HistoryField> processMap(String fieldName, Map<String, Object> before,
      Map<String, Object> after) {
    String left = stableMapString(before);
    String right = stableMapString(after);
    return left.equals(right)
        ? Optional.empty()
        : Optional.of(HistoryField.of(fieldName, left, right));
  }

  /**
   * Builds a history field for a changed name value.
   *
   * @param oldName previous name
   * @param newName current name
   * @return optional history field present if name changed and newName is not empty
   */
  public static Optional<HistoryField> processName(String oldName, String newName) {
    if (!Strings.isNullOrEmpty(newName) && !oldName.equals(newName)) {
      return Optional.of(HistoryField.of(NAME, oldName, newName));
    }
    return Optional.empty();
  }

  /**
   * Builds a history field describing description change. Nulls are treated as empty.
   *
   * @param oldDescription previous description
   * @param newDescription current description
   * @return optional history field present if values differ
   */
  public static Optional<HistoryField> processDescription(String oldDescription, String newDescription) {
    oldDescription = Strings.nullToEmpty(oldDescription);
    newDescription = Strings.nullToEmpty(newDescription);
    if (!newDescription.equals(oldDescription)) {
      return Optional.of(HistoryField.of(DESCRIPTION, oldDescription, newDescription));
    }
    return Optional.empty();
  }

  /**
   * Generic string field comparator producing a history field if values differ. Nulls are transformed to empty strings
   * before comparison.
   *
   * @param fieldName field key to use in history field
   * @param before    previous value
   * @param after     current value
   * @return optional history field present if values differ
   */
  public static Optional<HistoryField> processString(String fieldName, String before, String after) {
    before = Strings.nullToEmpty(before);
    after = Strings.nullToEmpty(after);
    if (!after.equals(before)) {
      return Optional.of(HistoryField.of(fieldName, before, after));
    }
    return Optional.empty();
  }

  /**
   * Boolean comparator producing a history field when the value changes.
   *
   * @param type     history field key
   * @param previous previous boolean value
   * @param current  current boolean value
   * @return optional history field present if values differ
   */
  public static Optional<HistoryField> processBoolean(String type, boolean previous, boolean current) {
    if (previous != current) {
      return Optional.of(HistoryField.of(type, String.valueOf(previous), String.valueOf(current)));
    }
    return Optional.empty();
  }

  public static Optional<HistoryField> processField(String fieldName, Object before, Object after) {
    String beforeStr = Objects.toString(before, "");
    String afterStr = Objects.toString(after, "");
    if (!afterStr.equals(beforeStr)) {
      return Optional.of(HistoryField.of(fieldName, beforeStr, afterStr));
    }
    return Optional.empty();
  }

  public static Optional<HistoryField> processParameter(Map<String, String> oldConfig,
      Map<String, String> newConfig,
      String parameterName) {
    String before = oldConfig.get(parameterName);
    String after = newConfig.get(parameterName);
    if (after != null && !after.equals(before)) {
      return Optional.of(HistoryField.of(parameterName, before, after));
    }
    return Optional.empty();
  }

  /**
   * Checks equality of two configuration maps limited to entries with the given prefix.
   *
   * @param before previous configuration
   * @param after  current configuration
   * @param prefix key prefix to filter by
   * @return true if filtered maps are equal
   */
  public static boolean configEquals(Map<String, String> before, Map<String, String> after, String prefix) {
    Map<String, String> beforeJobConfig = extractConfigByPrefix(before, prefix);
    Map<String, String> afterJobConfig = extractConfigByPrefix(after, prefix);
    return beforeJobConfig.equals(afterJobConfig);
  }

  /**
   * Opposite of {@link #configEquals(Map, Map, String)} for convenience.
   *
   * @param before previous configuration
   * @param after  current configuration
   * @param prefix key prefix to filter by
   * @return true if filtered maps are not equal
   */
  public static boolean configChanged(Map<String, String> before, Map<String, String> after, String prefix) {
    return !configEquals(before, after, prefix);
  }

  /**
   * Extracts entries of the provided configuration whose keys start with the given prefix.
   *
   * @param config source configuration map
   * @param prefix key prefix to filter by
   * @return map containing only entries with keys starting with the prefix
   */
  public static Map<String, String> extractConfigByPrefix(Map<String, String> config, String prefix) {
    return config.entrySet()
        .stream()
        .filter(entry -> entry.getKey().startsWith(prefix))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static List<String> normalizeList(List<String> input) {
    return Optional.ofNullable(input)
        .orElseGet(List::of)
        .stream()
        .map(v -> Objects.toString(v, EMPTY_STRING))
        .toList();
  }

  private static String stableMapString(Map<String, Object> map) {
    return Optional.ofNullable(map)
        .orElseGet(Map::of)
        .entrySet()
        .stream()
        .map(e -> e.getKey() + "=" + Objects.toString(e.getValue(), EMPTY_STRING))
        .collect(Collectors.joining(", ", "{", "}"));
  }
}
