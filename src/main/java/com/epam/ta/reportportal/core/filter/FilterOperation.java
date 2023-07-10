/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.epam.ta.reportportal.core.filter;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.FilterCondition.ConditionBuilder;
import java.util.Arrays;
import java.util.Optional;

/**
 * List operations for SearchCriteria
 *
 * @author Ryhor_Kukharenka
 */
public enum FilterOperation {

  EQ(FilterCondition.builder().withCondition(Condition.EQUALS)),
  NE(FilterCondition.builder().withCondition(Condition.NOT_EQUALS)),
  CNT(FilterCondition.builder().withCondition(Condition.CONTAINS)),
  NON_CNT(FilterCondition.builder().withCondition(Condition.CONTAINS).withNegative(true)),
  BTW(FilterCondition.builder().withCondition(Condition.BETWEEN)),
  IN(FilterCondition.builder().withCondition(Condition.IN));

  private final ConditionBuilder conditionBuilder;

  FilterOperation(ConditionBuilder conditionBuilder) {
    this.conditionBuilder = conditionBuilder;
  }

  public static Optional<FilterOperation> fromString(String string) {
    return Optional.ofNullable(string)
        .flatMap(str -> Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(str))
            .findAny());
  }

  public ConditionBuilder getConditionBuilder() {
    return conditionBuilder;
  }
}
