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

package com.epam.reportportal.base.infrastructure.persistence.util;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterTarget.FILTERED_QUERY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder.STATISTICS_KEY;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.JooqFieldNameTransformer.fieldName;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.jooq.impl.DSL.field;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.CriteriaHolder;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterTarget;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.StreamSupport;
import org.jooq.Field;
import org.jooq.SelectQuery;
import org.jooq.SortField;
import org.jooq.SortOrder;
import org.springframework.data.domain.Sort;

/**
 * Maps Spring {@link Sort} to jOOQ order expressions for filter queries.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public final class SortUtils {

  public static final BiFunction<Sort, FilterTarget, List<SortField<?>>> TO_SORT_FIELDS = (sort, filterTarget) -> ofNullable(
      sort).map(s -> StreamSupport
      .stream(s.spliterator(), false)
      .map(order -> {
        BusinessRule.expect(filterTarget, Objects::nonNull)
            .verify(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR, "Provided value shouldn't be null");
        CriteriaHolder criteria = filterTarget.getCriteriaByFilter(order.getProperty())
            .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_SORTING_PARAMETERS,
                order.getProperty()));

        if (criteria.getFilterCriteria().startsWith(STATISTICS_KEY)) {
          return fieldName(FILTERED_QUERY, criteria.getFilterCriteria()).sort(
              order.getDirection().isDescending() ?
                  SortOrder.DESC :
                  SortOrder.ASC);
        } else {
          return field(criteria.getQueryCriteria()).sort(
              order.getDirection().isDescending() ? SortOrder.DESC : SortOrder.ASC);
        }
      })
      .collect(toList())).orElseGet(Collections::emptyList);

  public static final BiFunction<Sort, SelectQuery<?>, List<SortField<?>>> RESOLVE_SORT_FIELDS = (sort, query) ->
      ofNullable(sort)
          .filter(Sort::isSorted)
          .map(s -> s.stream()
              .map(order -> {
                Field<?> field = findFieldInQuery(query, order.getProperty());
                if (field == null) {
                  throw new IllegalArgumentException("Unknown sort property: " + order.getProperty());
                }
                return order.isAscending() ? field.asc() : field.desc();
              })
              .collect(toList()))
          .orElse(Collections.emptyList());

  private SortUtils() {
    //static only
  }

  private static Field<?> findFieldInQuery(SelectQuery<?> query, String property) {
    return query.getSelect().stream()
        .filter(f -> matchesField(f, property))
        .findFirst()
        .orElse(null);
  }

  private static boolean matchesField(Field<?> field, String property) {
    String fieldName = field.getName();

    return property.equalsIgnoreCase(fieldName);
  }
}
