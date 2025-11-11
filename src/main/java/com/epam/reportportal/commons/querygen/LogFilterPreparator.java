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

package com.epam.reportportal.commons.querygen;

import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.LogCriteriaConstant.CRITERIA_LOG_LEVEL;

import com.epam.reportportal.infrastructure.persistence.commons.querygen.CompositeFilterCondition;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.infrastructure.persistence.service.LogTypeResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Prepares filters for log queries by transforming log level names to their integer values.
 */
@Component
@RequiredArgsConstructor
public class LogFilterPreparator {

  private final LogTypeResolver logTypeResolver;

  /**
   * Prepares a Filter for log queries by transforming log level names to integers.
   *
   * @param filter    the filter to prepare
   * @param projectId the project ID for resolving custom log types
   * @return a new filter with transformed log level values, or the original filter unchanged
   */
  public Filter prepare(Filter filter, Long projectId) {
    if (!hasLogLevelConditions(filter)) {
      return filter;
    }

    List<ConvertibleCondition> transformedConditions = filter.getFilterConditions().stream()
        .map(condition -> transformCondition(condition, projectId))
        .collect(Collectors.toCollection(ArrayList::new));

    return Objects.nonNull(filter.getId())
        ? new Filter(filter.getId(), filter.getTarget().getClazz(), transformedConditions)
        : new Filter(filter.getTarget().getClazz(), transformedConditions);
  }

  /**
   * Prepares a Queryable for log queries. If the queryable is a Filter, transforms log level values. Otherwise, returns
   * the queryable unchanged.
   *
   * @param queryable the queryable to prepare
   * @param projectId the project ID for resolving custom log types
   * @return a prepared queryable with transformed log level values if applicable
   */
  public Queryable prepare(Queryable queryable, Long projectId) {
    return queryable instanceof Filter filter
        ? prepare(filter, projectId)
        : queryable;
  }

  private boolean hasLogLevelConditions(Filter filter) {
    return filter.getFilterConditions().stream()
        .anyMatch(this::isLogLevelCondition);
  }

  private boolean isLogLevelCondition(ConvertibleCondition condition) {
    return switch (condition) {
      case FilterCondition fc -> CRITERIA_LOG_LEVEL.equalsIgnoreCase(fc.getSearchCriteria());
      case CompositeFilterCondition composite -> composite.getConditions().stream()
          .anyMatch(this::isLogLevelCondition);
      default -> false;
    };
  }

  private ConvertibleCondition transformCondition(ConvertibleCondition condition, Long projectId) {
    return switch (condition) {
      case FilterCondition fc -> transformFilterCondition(fc, projectId);
      case CompositeFilterCondition composite -> transformCompositeCondition(composite, projectId);
      default -> condition;
    };
  }

  private FilterCondition transformFilterCondition(FilterCondition condition, Long projectId) {
    if (!CRITERIA_LOG_LEVEL.equalsIgnoreCase(condition.getSearchCriteria())) {
      return condition;
    }

    int levelInt = logTypeResolver.resolveLogLevelFromName(projectId, condition.getValue());

    return new FilterCondition(condition.getOperator(), condition.getCondition(),
        condition.isNegative(), String.valueOf(levelInt), condition.getSearchCriteria());
  }

  private CompositeFilterCondition transformCompositeCondition(CompositeFilterCondition composite,
      Long projectId) {
    List<ConvertibleCondition> transformedConditions = composite.getConditions().stream()
        .map(condition -> transformCondition(condition, projectId))
        .collect(Collectors.toCollection(ArrayList::new));
    return new CompositeFilterCondition(transformedConditions, composite.getOperator());
  }
}
