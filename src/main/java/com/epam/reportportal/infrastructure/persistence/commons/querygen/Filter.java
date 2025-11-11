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

import com.epam.reportportal.infrastructure.persistence.commons.querygen.query.QuerySupplier;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jooq.Operator;
import org.jooq.impl.DSL;
import org.springframework.util.Assert;

/**
 * Filter for building queries to database. Contains CriteriaHolder which is mapping between request search criterias
 * and DB search criterias and value to be filtered
 *
 * @author Andrei Varabyeu
 */
public class Filter implements Serializable, Queryable {

  private Long id;

  private FilterTarget target;

  private List<ConvertibleCondition> filterConditions;

  /**
   * This constructor uses during serialization to database.
   */
  @SuppressWarnings("unused")
  private Filter() {

  }

  public Filter(Class<?> target, Condition condition, boolean negative, String value,
      String searchCriteria) {
    this(FilterTarget.findByClass(target),
        Lists.newArrayList(new FilterCondition(condition, negative, value, searchCriteria)));
  }

  public Filter(Class<?> target, List<ConvertibleCondition> filterConditions) {
    this(FilterTarget.findByClass(target), filterConditions);
  }

  public Filter(Long filterId, Class<?> target, Condition condition, boolean negative, String value,
      String searchCriteria) {
    this(filterId,
        FilterTarget.findByClass(target),
        Lists.newArrayList(new FilterCondition(condition, negative, value, searchCriteria))
    );
  }

  public Filter(Long filterId, Class<?> target, List<ConvertibleCondition> filterConditions) {
    this(filterId, FilterTarget.findByClass(target), filterConditions);
  }

  protected Filter(Long id, FilterTarget target, List<ConvertibleCondition> filterConditions) {
    Assert.notNull(id, "Filter id shouldn't be null");
    Assert.notNull(target, "Filter target shouldn't be null");
    Assert.notNull(filterConditions, "Conditions value shouldn't be null");
    this.id = id;
    this.target = target;
    this.filterConditions = filterConditions;
  }

  protected Filter(FilterTarget target, List<ConvertibleCondition> filterConditions) {
    Assert.notNull(target, "Filter target shouldn't be null");
    Assert.notNull(filterConditions, "Conditions value shouldn't be null");
    this.target = target;
    this.filterConditions = filterConditions;
  }

  public static FilterBuilder builder() {
    return new FilterBuilder();
  }

  public Long getId() {
    return id;
  }

  @Override
  public FilterTarget getTarget() {
    return target;
  }

  @Override
  public List<ConvertibleCondition> getFilterConditions() {
    return filterConditions;
  }

  public Filter withCondition(ConvertibleCondition filterCondition) {
    this.filterConditions.add(filterCondition);
    return this;
  }

  public Filter withConditions(List<ConvertibleCondition> conditions) {
    this.filterConditions.addAll(conditions);
    return this;
  }

  @Override
  public QuerySupplier toQuery() {
    QueryBuilder queryBuilder = QueryBuilder.newBuilder(this.target);
    Map<ConditionType, org.jooq.Condition> conditions = toCondition();
    return queryBuilder.addCondition(conditions.get(ConditionType.WHERE))
        .addHavingCondition(conditions.get(ConditionType.HAVING))
        .getQuerySupplier();
  }

  @Override
  public Map<ConditionType, org.jooq.Condition> toCondition() {
    Map<ConditionType, org.jooq.Condition> resultedConditions = new HashMap<>();
    for (ConvertibleCondition filterCondition : filterConditions) {
      filterCondition.toCondition(this.target)
          .forEach((key, value) -> addTransformedCondition(resultedConditions,
              filterCondition.getOperator(), value, key));
    }
    return resultedConditions;
  }

  /**
   * Transforms {@link FilterCondition} into {@link org.jooq.Condition} and adds it to existed {@link Condition}
   * according the {@link ConditionType} with {@link FilterCondition#getOperator()} {@link Operator}
   *
   * @param resultedConditions Resulted map of conditions divided into {@link ConditionType}
   * @param conditionType      {@link ConditionType}
   * @return Updated map of conditions
   */
  private Map<ConditionType, org.jooq.Condition> addTransformedCondition(
      Map<ConditionType, org.jooq.Condition> resultedConditions,
      Operator operator, org.jooq.Condition condition, ConditionType conditionType) {
    org.jooq.Condition composite = resultedConditions.getOrDefault(conditionType,
        DSL.noCondition());
    composite = DSL.condition(operator, composite, condition);
    resultedConditions.put(conditionType, composite);
    return resultedConditions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Filter filter = (Filter) o;
    return Objects.equals(id, filter.id) && target == filter.target && Objects.equals(
        filterConditions, filter.filterConditions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, target, filterConditions);
  }

  @Override
  public String toString() {
    return "Filter{" + "id=" + id + ", target=" + target + ", filterConditions=" + filterConditions
        + '}';
  }

  public boolean replaceSearchCriteria(FilterCondition oldCondition, FilterCondition newCondition) {
    if (oldCondition == null || newCondition == null) {
      return false;
    }

    return replaceSearchCriteria(oldCondition, newCondition, filterConditions);
  }

  private boolean replaceSearchCriteria(FilterCondition oldCondition, FilterCondition newCondition,
      List<ConvertibleCondition> filterConditionList) {
    for (int i = 0, filterConditionListSize = filterConditionList.size();
        i < filterConditionListSize; i++) {
      ConvertibleCondition filterCondition = filterConditionList.get(i);
      if (filterCondition instanceof FilterCondition) {
        FilterCondition filterCondition1 = (FilterCondition) filterCondition;
        if (filterCondition1.getCondition() == oldCondition.getCondition() &&
            filterCondition1.isNegative() == oldCondition.isNegative() &&
            Objects.equals(filterCondition1.getSearchCriteria(), oldCondition.getSearchCriteria())
        ) {
          filterConditionList.set(i, newCondition);
          return true;
        }
      } else if (filterCondition instanceof CompositeFilterCondition) {
        if (replaceSearchCriteria(oldCondition, newCondition,
            ((CompositeFilterCondition) filterCondition).getConditions())) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Builder for {@link Filter}
   */
  public static class FilterBuilder {

    private Class<?> target;

    private List<ConvertibleCondition> conditions = Lists.newArrayList();

    private FilterBuilder() {

    }

    public FilterBuilder withTarget(Class<?> target) {
      this.target = target;
      return this;
    }

    public FilterBuilder withCondition(ConvertibleCondition condition) {
      this.conditions.add(condition);
      return this;
    }

    public Filter build() {
      List<ConvertibleCondition> filterConditions = Lists.newArrayList();
      filterConditions.addAll(this.conditions);
      Preconditions.checkArgument(null != target, "FilterTarget should not be null");
      Preconditions.checkArgument(!filterConditions.isEmpty(),
          "Filter should contain at least one condition");
      return new Filter(FilterTarget.findByClass(target), filterConditions);
    }
  }
}
