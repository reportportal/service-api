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

package com.epam.reportportal.infrastructure.persistence.commons.querygen;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jooq.Condition;
import org.jooq.Operator;
import org.jooq.impl.DSL;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class CompositeFilterCondition implements ConvertibleCondition {

  private List<ConvertibleCondition> conditions;

  private Operator operator;

  public CompositeFilterCondition(List<ConvertibleCondition> conditions, Operator operator) {
    this.conditions = conditions;
    this.operator = operator;
  }

  public CompositeFilterCondition(List<ConvertibleCondition> conditions) {
    this.conditions = conditions;
    this.operator = Operator.AND;
  }

  @Override
  public Map<ConditionType, Condition> toCondition(FilterTarget filterTarget) {

    Map<ConditionType, Condition> result = Maps.newHashMapWithExpectedSize(
        ConditionType.values().length);

    conditions.forEach(c -> {
      Map<ConditionType, Condition> conditionMap = c.toCondition(filterTarget);
      conditionMap.forEach((key, value) -> {
        Condition condition = result.getOrDefault(key, DSL.noCondition());
        result.put(key, DSL.condition(c.getOperator(), condition, value));
      });
    });

    return result;
  }

  public List<ConvertibleCondition> getConditions() {
    return conditions;
  }

  public void setConditions(List<ConvertibleCondition> conditions) {
    this.conditions = conditions;
  }

  @Override
  public Operator getOperator() {
    return operator;
  }

  public void setOperator(Operator operator) {
    this.operator = operator;
  }

  @Override
  public List<FilterCondition> getAllConditions() {
    return conditions.stream().map(ConvertibleCondition::getAllConditions)
        .flatMap(Collection::stream).collect(Collectors.toList());
  }
}
