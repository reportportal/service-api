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

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_CASE_DESCRIPTION;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_CASE_EXPECTED_RESULT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_CASE_INSTRUCTIONS;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_CASE_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_CASE_PRECONDITION;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_CASE;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.CompositeFilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Field;
import org.jooq.impl.DSL;

/**
 * Search of test cases by their content: precondition, instructions and expected result of the
 * default version (EPMRPP-123497).
 *
 * <p>All searches are "contains", ignoring case. Instructions and expected result cover both
 * scenario types: every step of a step-based scenario and the text of a text-based scenario.
 */
public final class TmsContentSearchFilterSupport {

  /**
   * Precondition of the default version of the test case of the outer query
   * ({@code tms_test_case}).
   */
  public static final String DEFAULT_VERSION_PRECONDITION =
      "(SELECT tms_cs_precondition.value"
          + " FROM tms_test_case_version tms_cs_version"
          + " JOIN tms_manual_scenario tms_cs_scenario"
          + " ON tms_cs_scenario.test_case_version_id = tms_cs_version.id"
          + " JOIN tms_manual_scenario_preconditions tms_cs_precondition"
          + " ON tms_cs_precondition.manual_scenario_id = tms_cs_scenario.id"
          + " WHERE tms_cs_version.test_case_id = tms_test_case.id"
          + " AND tms_cs_version.is_default = true)";

  /**
   * Instructions of the default version of the test case of the outer query: the instructions of
   * every step and the instructions of a text scenario, one value per line.
   */
  public static final String DEFAULT_VERSION_INSTRUCTIONS = scenarioText("instructions");

  /**
   * Expected result of the default version of the test case of the outer query: the expected
   * result of every step and the expected result of a text scenario, one value per line.
   */
  public static final String DEFAULT_VERSION_EXPECTED_RESULT = scenarioText("expected_result");

  /**
   * Folder criteria that search the test cases of a folder, and the test case value each one
   * matches. Used to count the matching test cases of a folder.
   */
  private static final Map<String, Field<String>> FOLDER_TEST_CASE_FIELDS = Map.of(
      CRITERIA_TMS_TEST_FOLDER_TEST_CASE_NAME, TMS_TEST_CASE.NAME,
      CRITERIA_TMS_TEST_FOLDER_TEST_CASE_DESCRIPTION, TMS_TEST_CASE.DESCRIPTION,
      CRITERIA_TMS_TEST_FOLDER_TEST_CASE_PRECONDITION,
      DSL.field(DEFAULT_VERSION_PRECONDITION, String.class),
      CRITERIA_TMS_TEST_FOLDER_TEST_CASE_INSTRUCTIONS,
      DSL.field(DEFAULT_VERSION_INSTRUCTIONS, String.class),
      CRITERIA_TMS_TEST_FOLDER_TEST_CASE_EXPECTED_RESULT,
      DSL.field(DEFAULT_VERSION_EXPECTED_RESULT, String.class)
  );

  private TmsContentSearchFilterSupport() {
  }

  private static String scenarioText(String column) {
    var defaultScenario = " FROM tms_test_case_version tms_cs_version"
        + " JOIN tms_manual_scenario tms_cs_scenario"
        + " ON tms_cs_scenario.test_case_version_id = tms_cs_version.id";
    var defaultVersionOfOuterTestCase = " WHERE tms_cs_version.test_case_id = tms_test_case.id"
        + " AND tms_cs_version.is_default = true";
    return "(SELECT string_agg(tms_cs_text.value, chr(10)) FROM ("
        + "SELECT tms_cs_step." + column + " AS value"
        + defaultScenario
        + " JOIN tms_step tms_cs_step"
        + " ON tms_cs_step.steps_manual_scenario_id = tms_cs_scenario.id"
        + defaultVersionOfOuterTestCase
        + " UNION ALL"
        + " SELECT tms_cs_text_scenario." + column + " AS value"
        + defaultScenario
        + " JOIN tms_text_manual_scenario tms_cs_text_scenario"
        + " ON tms_cs_text_scenario.manual_scenario_id = tms_cs_scenario.id"
        + defaultVersionOfOuterTestCase
        + ") tms_cs_text)";
  }

  /**
   * Combines repeated "contains" parameters of the criteria with AND.
   *
   * <p>The filter resolver keeps only the first value of a repeated request parameter. This method
   * adds one "contains" condition for each further non-blank value of
   * {@code filter.cnt.<criteria>}, so a test case must contain all values. It lets the page
   * search and a search condition of the filter panel use the same criteria at the same time.
   *
   * @param filter         filter built from the request
   * @param parameterValues all values of a request parameter by its name, for example
   *                       {@code request::getParameterValues}
   * @param criteria       filter criteria names, for example "name", "instructions"
   * @return the filter to use; the same filter when no parameter is repeated
   */
  public static Filter withAllValues(Filter filter, Function<String, String[]> parameterValues,
      String... criteria) {
    if (filter == null) {
      return null;
    }
    List<ConvertibleCondition> extra = new ArrayList<>();
    for (var criterion : criteria) {
      var values = parameterValues.apply("filter." + Condition.CNT + criterion);
      if (values == null || values.length < 2) {
        continue;
      }
      Arrays.stream(values)
          .skip(1)
          .filter(StringUtils::isNotBlank)
          .forEach(value -> extra.add(
              new FilterCondition(Condition.CONTAINS, false, value, criterion)));
    }
    if (extra.isEmpty()) {
      return filter;
    }
    List<ConvertibleCondition> conditions = new ArrayList<>(filter.getFilterConditions());
    conditions.addAll(extra);
    return filter.getId() == null
        ? new Filter(filter.getTarget().getClazz(), conditions)
        : new Filter(filter.getId(), filter.getTarget().getClazz(), conditions);
  }

  /**
   * Conditions for a query that selects from {@code tms_test_case}: one "contains, ignoring case"
   * condition for every positive "contains" condition of the folder filter on the test case name,
   * description, precondition, instructions or expected result, also inside groups.
   *
   * @param filter folder filter
   * @return jOOQ conditions, combined with AND by the caller; empty when there are none
   */
  public static List<org.jooq.Condition> folderTestCaseConditions(Filter filter) {
    var result = new ArrayList<org.jooq.Condition>();
    collect(filter.getFilterConditions(), result);
    return result;
  }

  private static void collect(Collection<ConvertibleCondition> conditions,
      List<org.jooq.Condition> result) {
    for (var condition : conditions) {
      if (condition instanceof FilterCondition fc
          && fc.getCondition() == Condition.CONTAINS
          && !fc.isNegative()
          && StringUtils.isNotBlank(fc.getValue())) {
        var field = FOLDER_TEST_CASE_FIELDS.get(fc.getSearchCriteria());
        if (field != null) {
          result.add(field.containsIgnoreCase(fc.getValue()));
        }
      } else if (condition instanceof CompositeFilterCondition composite) {
        collect(composite.getConditions(), result);
      }
    }
  }
}
