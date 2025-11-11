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

import static org.postgresql.shaded.com.ongres.scram.common.util.Preconditions.checkArgument;

import com.epam.reportportal.infrastructure.persistence.commons.querygen.query.QuerySupplier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jooq.Condition;
import org.jooq.Operator;
import org.jooq.impl.DSL;

/**
 * Composite filter. Combines filters using {@link Operator} and builds query.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class CompositeFilter implements Queryable {

  private Operator operator;
  private Collection<Queryable> filters;
  private FilterTarget target;

  public CompositeFilter(Operator operator, Collection<Queryable> filters) {
    checkArgument(null != operator, "Operator is not specified");
    checkArgument(null != filters && !filters.isEmpty(), "Empty filter list");
    checkArgument(1 == filters.stream().map(Queryable::getTarget).distinct().count(),
        "Different targets");
    this.operator = operator;
    this.target = filters.iterator().next().getTarget();
    this.filters = filters;
  }

  public CompositeFilter(Operator operator, Queryable... filters) {
    this(operator, Arrays.asList(filters));
  }

  @Override
  public QuerySupplier toQuery() {
    QueryBuilder query = QueryBuilder.newBuilder(this.target);
    Map<ConditionType, Condition> conditions = toCondition();
    return query.addCondition(conditions.get(ConditionType.WHERE))
        .addHavingCondition(conditions.get(ConditionType.HAVING))
        .getQuerySupplier();
  }

  @Override
  public Map<ConditionType, Condition> toCondition() {
    Map<ConditionType, Condition> resultedConditions = new HashMap<>();
    for (Queryable filter : filters) {
      filter.toCondition().forEach((conditionType, condition) -> {
        Condition compositeCondition = resultedConditions.getOrDefault(conditionType,
            DSL.noCondition());
        resultedConditions.put(conditionType,
            DSL.condition(operator, compositeCondition, condition));
      });
    }
    return resultedConditions;
  }

  @Override
  public FilterTarget getTarget() {
    return target;
  }

  @Override
  public List<ConvertibleCondition> getFilterConditions() {
    return filters.stream().flatMap(it -> it.getFilterConditions().stream())
        .collect(Collectors.toList());
  }

  public boolean replaceSearchCriteria(FilterCondition oldCondition, FilterCondition newCondition) {
    if (oldCondition == null || newCondition == null) {
      return false;
    }

    for (Queryable filter : filters) {
      if (filter.replaceSearchCriteria(oldCondition, newCondition)) {
        return true;
      }
    }

    return false;
  }
}
