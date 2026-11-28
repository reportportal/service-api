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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.CompositeFilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import java.util.ArrayList;
import java.util.List;
import org.jooq.Operator;
import org.junit.jupiter.api.Test;

class TmsRequirementFilterSupportTest {

  private static final String CRITERIA = "requirement";

  @Test
  void normalize_TrimsDropsBlankAndCaseInsensitiveDuplicates() {
    assertEquals(List.of("EPMRPP-1", "login"),
        TmsRequirementFilterSupport.normalize(
            new String[]{" EPMRPP-1 ", "", "   ", null, "epmrpp-1", "login"}));
    assertEquals(List.of(), TmsRequirementFilterSupport.normalize(null));
  }

  @Test
  void withAnyRequirement_OneValue_FilterUnchanged() {
    var filter = filter(new FilterCondition(Condition.CONTAINS, false, "EPMRPP-1", CRITERIA));

    assertSame(filter,
        TmsRequirementFilterSupport.withAnyRequirement(filter, CRITERIA, new String[]{"EPMRPP-1"}));
    assertSame(filter,
        TmsRequirementFilterSupport.withAnyRequirement(filter, CRITERIA, new String[]{"a", " "}));
  }

  @Test
  void withAnyRequirement_SeveralValues_ReplacedWithOrGroup() {
    var priority = new FilterCondition(Condition.IN, false, "HIGH", "priority");
    var filter = filter(priority,
        new FilterCondition(Condition.CONTAINS, false, "EPMRPP-1", CRITERIA));

    var result = TmsRequirementFilterSupport.withAnyRequirement(filter, CRITERIA,
        new String[]{"EPMRPP-1", "EPMRPP-2"});

    assertEquals(2, result.getFilterConditions().size());
    assertSame(priority, result.getFilterConditions().get(0));
    var group = assertInstanceOf(CompositeFilterCondition.class,
        result.getFilterConditions().get(1));
    assertEquals(Operator.AND, group.getOperator());
    group.getConditions().forEach(c -> assertEquals(Operator.OR, c.getOperator()));
    assertEquals(List.of("EPMRPP-1", "EPMRPP-2"), group.getConditions().stream()
        .map(c -> ((FilterCondition) c).getValue())
        .toList());
    assertEquals(List.of("EPMRPP-1", "EPMRPP-2"),
        TmsRequirementFilterSupport.extractValues(result, CRITERIA));
  }

  @Test
  void extractValues_IgnoresNegativeAndOtherCriteria() {
    var filter = filter(
        new FilterCondition(Condition.CONTAINS, true, "excluded", CRITERIA),
        new FilterCondition(Condition.CONTAINS, false, "other", "name"),
        new FilterCondition(Condition.CONTAINS, false, "EPMRPP-1", CRITERIA));

    assertEquals(List.of("EPMRPP-1"), TmsRequirementFilterSupport.extractValues(filter, CRITERIA));
  }

  private static Filter filter(ConvertibleCondition... conditions) {
    return new Filter(TmsTestCase.class, new ArrayList<>(List.of(conditions)));
  }
}
