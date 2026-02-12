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

package com.epam.reportportal.base.infrastructure.persistence.commons.querygen;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder.HAVING_CONDITION;

import com.epam.reportportal.base.infrastructure.persistence.commons.Preconditions;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.google.common.collect.Lists;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jooq.Operator;

/**
 * Filter condition class for filters specifics
 */
@Entity
@Table(name = "filter_condition", schema = "public")

public class FilterCondition implements ConvertibleCondition, Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false, precision = 64)
  private Long id;

  /**
   * Filter Condition
   */
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "condition")
  private Condition condition;

  /**
   * Value to be filtered
   */
  @Column(name = "value")
  private String value;

  /**
   * API Model Search Criteria
   */
  @Column(name = "search_criteria")
  private String searchCriteria;

  /**
   * Whether this is 'NOT' filter
   */
  @Column(name = "negative")
  private boolean negative;

  /**
   * Whether this is 'AND' or 'OR' filter
   */
  @Transient
  private Operator operator = Operator.AND;

  public FilterCondition() {
  }

  public FilterCondition(Condition condition, boolean negative, String value,
      String searchCriteria) {
    this.condition = condition;
    this.value = value;
    this.searchCriteria = searchCriteria;
    this.negative = negative;
  }

  public FilterCondition(Operator operator, Condition condition, boolean negative, String value,
      String searchCriteria) {
    this.condition = condition;
    this.value = value;
    this.searchCriteria = searchCriteria;
    this.negative = negative;
    this.operator = operator;
  }

  public static ConditionBuilder builder() {
    return new ConditionBuilder();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Condition getCondition() {
    return condition;
  }

  public String getSearchCriteria() {
    return searchCriteria;
  }

  public String getValue() {
    return value;
  }

  public boolean isNegative() {
    return negative;
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
    return Lists.newArrayList(this);
  }

  @Override
  public Map<ConditionType, org.jooq.Condition> toCondition(FilterTarget filterTarget) {

    Optional<CriteriaHolder> criteriaHolder = filterTarget.getCriteriaByFilter(searchCriteria);

    BusinessRule.expect(criteriaHolder,
            Preconditions.IS_PRESENT)
        .verify(ErrorType.INCORRECT_FILTER_PARAMETERS,
            Suppliers.formattedSupplier("Filter parameter {} is not defined", searchCriteria)
        );

    org.jooq.Condition condition = this.condition.toCondition(this, criteriaHolder.get());

    /* Does FilterCondition contains negative=true? */
    if (negative) {
      condition = condition.not();
    }
    if (HAVING_CONDITION.test(this, filterTarget)) {
      return Collections.singletonMap(ConditionType.HAVING, condition);
    } else {
      return Collections.singletonMap(ConditionType.WHERE, condition);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((condition == null) ? 0 : condition.hashCode());
    result = prime * result + (negative ? 1231 : 1237);
    result = prime * result + ((searchCriteria == null) ? 0 : searchCriteria.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FilterCondition other = (FilterCondition) obj;
    if (condition != other.condition) {
      return false;
    }
    if (negative != other.negative) {
      return false;
    }
    if (searchCriteria == null) {
      if (other.searchCriteria != null) {
        return false;
      }
    } else if (!searchCriteria.equals(other.searchCriteria)) {
      return false;
    }
    if (value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!value.equals(other.value)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("FilterCondition {").append("condition = ")
        .append(condition)
        .append(", value = ")
        .append(value)
        .append(", searchCriteria = ")
        .append(searchCriteria)
        .append(", negative = ")
        .append(negative)
        .append("}");
    return sb.toString();
  }

  /**
   * Builder for {@link FilterCondition}
   */
  public static class ConditionBuilder {

    private Condition condition;

    private boolean negative;

    private String value;

    private String searchCriteria;

    private Operator operator = Operator.AND;

    private ConditionBuilder() {

    }

    public ConditionBuilder withCondition(Condition condition) {
      this.condition = condition;
      return this;
    }

    public ConditionBuilder withNegative(boolean negative) {
      this.negative = negative;
      return this;
    }

    public ConditionBuilder withValue(String value) {
      this.value = value;
      return this;
    }

    public ConditionBuilder withSearchCriteria(String searchCriteria) {
      this.searchCriteria = searchCriteria;
      return this;
    }

    public ConditionBuilder withOperator(Operator operator) {
      this.operator = operator;
      return this;
    }

    public ConditionBuilder eq(String searchCriteria, String value) {
      return withCondition(Condition.EQUALS).withSearchCriteria(searchCriteria).withValue(value);
    }

    public ConditionBuilder in(String searchCriteria, List<?> value) {
      return withSearchCriteria(searchCriteria).withCondition(Condition.IN)
          .withValue(value.stream().map(Object::toString).collect(Collectors.joining(",")));
    }

    public FilterCondition build() {
      BusinessRule.expect(condition, Objects::nonNull)
          .verify(ErrorType.BAD_REQUEST_ERROR, "Condition should not be null");
      BusinessRule.expect(value, Objects::nonNull)
          .verify(ErrorType.BAD_REQUEST_ERROR, "Value should not be null");
      BusinessRule.expect(searchCriteria, Objects::nonNull)
          .verify(ErrorType.BAD_REQUEST_ERROR, "Search criteria should not be null");
      BusinessRule.expect(condition, c -> c != Condition.EQUALS || !negative)
          .verify(ErrorType.BAD_REQUEST_ERROR, "Use 'ne' instead of '!eq");
      return new FilterCondition(operator, condition, negative, value, searchCriteria);
    }
  }
}
