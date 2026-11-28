/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.dao.tms.util;

import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_MANUAL_SCENARIO;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_MANUAL_SCENARIO_REQUIREMENT;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_CASE_VERSION;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.CompositeFilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.jooq.Field;
import org.jooq.Operator;
import org.jooq.impl.DSL;

/**
 * Filtering of test cases by the Requirements field of their default version.
 *
 * <p>A value matches a test case when at least one requirement of the default version contains
 * it, ignoring case. Several values are combined with OR.
 */
public final class TmsRequirementFilterSupport {

  /**
   * Requirement values of the default version of the test case of the outer query
   * ({@code tms_test_case}), one value per line. It is the query criteria of the "requirement"
   * filter of test cases and of the "testCaseRequirement" filter of folders.
   */
  public static final String DEFAULT_VERSION_REQUIREMENTS =
      "(SELECT string_agg(tms_req_value.value, chr(10))"
          + " FROM tms_test_case_version tms_req_version"
          + " JOIN tms_manual_scenario tms_req_scenario"
          + " ON tms_req_scenario.test_case_version_id = tms_req_version.id"
          + " JOIN tms_manual_scenario_requirement tms_req_value"
          + " ON tms_req_value.manual_scenario_id = tms_req_scenario.id"
          + " WHERE tms_req_version.test_case_id = tms_test_case.id"
          + " AND tms_req_version.is_default = true)";

  private TmsRequirementFilterSupport() {
  }

  /**
   * Trims the values, drops blank ones and drops duplicates that differ only in case. The order
   * of the first occurrences is kept.
   *
   * @param rawValues values from the request, may be {@code null}
   * @return normalized values, never {@code null}
   */
  public static List<String> normalize(String[] rawValues) {
    if (rawValues == null) {
      return List.of();
    }
    var unique = new LinkedHashMap<String, String>();
    for (var raw : rawValues) {
      if (raw == null) {
        continue;
      }
      var value = raw.trim();
      if (!value.isEmpty()) {
        unique.putIfAbsent(value.toLowerCase(Locale.ROOT), value);
      }
    }
    return List.copyOf(unique.values());
  }

  /**
   * Combines several values of a "contains" requirement criterion with OR.
   *
   * <p>The filter resolver keeps only the first value of a repeated request parameter. This method
   * replaces the positive "contains" conditions of the criterion with one OR group of all given
   * values. With fewer than two values the filter does not change.
   *
   * @param filter    filter built from the request
   * @param criteria  filter criteria name, for example "requirement"
   * @param rawValues all values of the request parameter {@code filter.cnt.<criteria>}
   * @return the filter to use
   */
  public static Filter withAnyRequirement(Filter filter, String criteria, String[] rawValues) {
    var values = normalize(rawValues);
    if (filter == null || values.size() < 2) {
      return filter;
    }
    List<ConvertibleCondition> conditions = new ArrayList<>();
    for (var condition : filter.getFilterConditions()) {
      if (!isPositiveContains(condition, criteria)) {
        conditions.add(condition);
      }
    }
    List<ConvertibleCondition> anyOf = new ArrayList<>();
    // The operator of each child joins it to the previous children; the group joins with AND
    values.forEach(value -> anyOf.add(
        new FilterCondition(Operator.OR, Condition.CONTAINS, false, value, criteria)));
    conditions.add(new CompositeFilterCondition(anyOf, Operator.AND));
    return filter.getId() == null
        ? new Filter(filter.getTarget().getClazz(), conditions)
        : new Filter(filter.getId(), filter.getTarget().getClazz(), conditions);
  }

  /**
   * Collects the values of positive "contains" conditions of the criterion, also inside OR groups.
   *
   * @param filter   filter
   * @param criteria filter criteria name
   * @return normalized values, never {@code null}
   */
  public static List<String> extractValues(Filter filter, String criteria) {
    var values = new ArrayList<String>();
    collect(filter.getFilterConditions(), criteria, values);
    return normalize(values.toArray(String[]::new));
  }

  /**
   * Condition for a query that selects from {@code tms_test_case}: at least one requirement of the
   * default version contains one of the values, ignoring case.
   *
   * @param testCaseId test case id field of the outer query
   * @param values     normalized values, not empty
   * @return jOOQ condition
   */
  public static org.jooq.Condition anyRequirementContains(Field<Long> testCaseId,
      Collection<String> values) {
    var valueMatches = values.stream()
        .map(TMS_MANUAL_SCENARIO_REQUIREMENT.VALUE::containsIgnoreCase)
        .toList();
    return DSL.exists(DSL.selectOne()
        .from(TMS_TEST_CASE_VERSION)
        .join(TMS_MANUAL_SCENARIO)
        .on(TMS_MANUAL_SCENARIO.TEST_CASE_VERSION_ID.eq(TMS_TEST_CASE_VERSION.ID))
        .join(TMS_MANUAL_SCENARIO_REQUIREMENT)
        .on(TMS_MANUAL_SCENARIO_REQUIREMENT.MANUAL_SCENARIO_ID.eq(TMS_MANUAL_SCENARIO.ID))
        .where(TMS_TEST_CASE_VERSION.TEST_CASE_ID.eq(testCaseId))
        .and(TMS_TEST_CASE_VERSION.IS_DEFAULT.isTrue())
        .and(DSL.or(valueMatches)));
  }

  private static void collect(List<ConvertibleCondition> conditions, String criteria,
      List<String> values) {
    for (var condition : conditions) {
      if (isPositiveContains(condition, criteria)) {
        values.add(((FilterCondition) condition).getValue());
      } else if (condition instanceof CompositeFilterCondition composite) {
        collect(composite.getConditions(), criteria, values);
      }
    }
  }

  private static boolean isPositiveContains(ConvertibleCondition condition, String criteria) {
    return condition instanceof FilterCondition fc
        && Objects.equals(criteria, fc.getSearchCriteria())
        && fc.getCondition() == Condition.CONTAINS
        && !fc.isNegative();
  }
}
