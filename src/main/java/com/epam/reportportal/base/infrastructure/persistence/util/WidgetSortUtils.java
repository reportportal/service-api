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

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder.STATISTICS_KEY;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.SF_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.STATISTICS_COUNTER;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.STATISTICS_TABLE;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.JooqFieldNameTransformer.fieldName;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.CriteriaHolder;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterTarget;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.SortOrder;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Sort;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class WidgetSortUtils {

  public static final BiFunction<Sort, FilterTarget, List<SortField<Object>>> TO_SORT_FIELDS = (sort, filterTarget) -> ofNullable(
      sort).map(
      s -> StreamSupport.stream(s.spliterator(), false).map(order -> {
        BusinessRule.expect(filterTarget, Objects::nonNull)
            .verify(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR, "Provided value shouldn't be null");

        CriteriaHolder criteria;

        if (order.getProperty().startsWith(STATISTICS_KEY)) {
          criteria = new CriteriaHolder(
              order.getProperty(),
              DSL.coalesce(DSL.max(fieldName(STATISTICS_TABLE, STATISTICS_COUNTER))
                      .filterWhere(fieldName(STATISTICS_TABLE, SF_NAME).cast(String.class)
                          .eq(order.getProperty())), 0)
                  .toString(),
              Long.class
          );
        } else {
          criteria = filterTarget.getCriteriaByFilter(order.getProperty())
              .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_SORTING_PARAMETERS,
                  order.getProperty()));
        }

        return field(criteria.getQueryCriteria()).sort(
            order.getDirection().isDescending() ? SortOrder.DESC : SortOrder.ASC);
      }).collect(toList())).orElseGet(Collections::emptyList);
  public static final BiFunction<String, SortField<?>, SortField<?>> CUSTOM_TABLE_SORT_CONVERTER = (table, sort) -> {

    if (sort.getName().contains(STATISTICS_TABLE)) {
      return sort;
    }
    String[] qualifiedName = sort.getName().split("\\.");
    String sortField = StringUtils.remove(qualifiedName[qualifiedName.length - 1], '"');

    return field(name(table, sortField)).sort(sort.getOrder());

  };

  private WidgetSortUtils() {

    //static only
  }

  public static BiFunction<Sort, String, List<SortField<Object>>> sortingTransformer(
      FilterTarget filterTarget) {
    return (sort, tableName) -> ofNullable(sort).map(
        s -> StreamSupport.stream(sort.spliterator(), false)
            .map(order -> transformToField(filterTarget, order, tableName).sort(
                order.getDirection().isDescending() ?
                    SortOrder.DESC :
                    SortOrder.ASC))
            .collect(Collectors.toList())).orElseGet(Collections::emptyList);
  }

  public static BiFunction<Sort, String, List<Field<Object>>> fieldTransformer(
      FilterTarget filterTarget) {
    return (sort, tableName) -> ofNullable(sort).map(
        s -> StreamSupport.stream(sort.spliterator(), false)
            .map(order -> transformToField(filterTarget, order, tableName))
            .collect(Collectors.toList())).orElseGet(Collections::emptyList);
  }

  private static Field<Object> transformToField(FilterTarget filterTarget, Sort.Order order,
      String tableName) {
    BusinessRule.expect(filterTarget, Objects::nonNull)
        .verify(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR, "Provided value shouldn't be null");

    String filterCriteria;

    if (order.getProperty().startsWith(STATISTICS_KEY)) {
      filterCriteria = order.getProperty();
    } else {
      filterCriteria = filterTarget.getCriteriaByFilter(order.getProperty())
          .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_SORTING_PARAMETERS,
              order.getProperty()))
          .getFilterCriteria();
    }

    return field(name(tableName, filterCriteria));
  }
}
