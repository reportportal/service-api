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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.CompositeFilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jooq.Operator;
import org.junit.jupiter.api.Test;

class TmsContentSearchFilterSupportTest {

  private static Filter testCaseFilter(ConvertibleCondition... conditions) {
    return new Filter(TmsTestCase.class, new ArrayList<>(List.of(conditions)));
  }

  @Test
  void withAllValues_AddsAContainsConditionForEveryFurtherNonBlankValue() {
    var filter = testCaseFilter(
        new FilterCondition(Condition.CONTAINS, false, "login", "name"),
        new FilterCondition(Condition.IN, false, "HIGH", "priority"));
    Map<String, String[]> parameters = Map.of(
        "filter.cnt.name", new String[]{"login", " ", "admin"},
        "filter.cnt.instructions", new String[]{"open"});

    var result = TmsContentSearchFilterSupport.withAllValues(filter, parameters::get, "name",
        "instructions");

    var conditions = result.getFilterConditions();
    assertEquals(3, conditions.size());
    var added = (FilterCondition) conditions.get(2);
    assertEquals("name", added.getSearchCriteria());
    assertEquals("admin", added.getValue());
    assertEquals(Condition.CONTAINS, added.getCondition());
    assertEquals(Operator.AND, added.getOperator());
  }

  @Test
  void withAllValues_KeepsTheFilterWhenNoParameterIsRepeated() {
    var filter = testCaseFilter(new FilterCondition(Condition.CONTAINS, false, "x", "name"));
    Map<String, String[]> parameters = Map.of("filter.cnt.name", new String[]{"x"});

    assertSame(filter, TmsContentSearchFilterSupport.withAllValues(filter, parameters::get,
        "name", "instructions"));
  }

  @Test
  void withAllValues_IgnoresRepeatedValuesOfCriteriaNotListed() {
    // e.g. "requirement" (EPMRPP-123492) keeps its own OR handling
    var filter = testCaseFilter(new FilterCondition(Condition.CONTAINS, false, "A", "requirement"));
    Map<String, String[]> parameters = Map.of(
        "filter.cnt.requirement", new String[]{"A", "B"},
        "filter.cnt.name", new String[]{"login"});

    assertSame(filter, TmsContentSearchFilterSupport.withAllValues(filter, parameters::get,
        "name", "description", "precondition", "instructions", "expectedResult"));
  }

  @Test
  void folderTestCaseConditions_OnlyPositiveContainsOfContentCriteria() {
    var filter = new Filter(TmsTestFolder.class, new ArrayList<>(List.of(
        new FilterCondition(Condition.CONTAINS, false, "step", "testCaseInstructions"),
        new FilterCondition(Condition.CONTAINS, true, "negated", "testCaseExpectedResult"),
        new FilterCondition(Condition.CONTAINS, false, "folder", "name"),
        new FilterCondition(Condition.IN, false, "HIGH", "testCasePriority"),
        new CompositeFilterCondition(List.of(
            new FilterCondition(Condition.CONTAINS, false, "ready", "testCasePrecondition"),
            new FilterCondition(Condition.CONTAINS, false, "Login", "testCaseName"))))));

    var conditions = TmsContentSearchFilterSupport.folderTestCaseConditions(filter);

    assertEquals(3, conditions.size());
    assertTrue(conditions.get(0).toString().contains("tms_step"));
    assertTrue(conditions.get(1).toString().contains("tms_manual_scenario_preconditions"));
    assertTrue(conditions.get(2).toString().contains("\"name\""));
  }
}
