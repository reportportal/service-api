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

import static com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterTarget.FILTERED_QUERY;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.reportportal.infrastructure.persistence.dao.util.JooqFieldNameTransformer.fieldName;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JLaunch.LAUNCH;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JStatistics.STATISTICS;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JStatisticsField.STATISTICS_FIELD;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JTestItem.TEST_ITEM;
import static java.util.Optional.ofNullable;
import static org.jooq.impl.DSL.field;

import com.epam.reportportal.infrastructure.persistence.commons.querygen.query.JoinEntity;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.query.QuerySupplier;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.StreamSupport;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.GroupField;
import org.jooq.JoinType;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.SortOrder;
import org.jooq.TableLike;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * PostgreSQL query builder using JOOQ. Constructs PostgreSQL {@link Query} by provided filters.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class QueryBuilder {

  /**
   * Key word for statistics criteria. Query builder works a little bit in another way with statistics criteria. It
   * implements kind of pivot using PostgerSQL possibilities
   */
  public final static String STATISTICS_KEY = "statistics";
  private final static Map<FilterTarget, JoinEntity> STATISTICS_TARGET_MAPPING = ImmutableMap.<FilterTarget, JoinEntity>builder()
      .put(
          FilterTarget.LAUNCH_TARGET,
          JoinEntity.of(STATISTICS, JoinType.LEFT_OUTER_JOIN, LAUNCH.ID.eq(STATISTICS.LAUNCH_ID))
      )
      .put(FilterTarget.TEST_ITEM_TARGET,
          JoinEntity.of(STATISTICS, JoinType.LEFT_OUTER_JOIN,
              TEST_ITEM.ITEM_ID.eq(STATISTICS.ITEM_ID))
      )
      .build();
  /**
   * Conditions that should be applied with HAVING
   */
  private static final List<com.epam.reportportal.infrastructure.persistence.commons.querygen.Condition> HAVING_CONDITIONS = ImmutableList.<com.epam.reportportal.infrastructure.persistence.commons.querygen.Condition>builder()
      .add(com.epam.reportportal.infrastructure.persistence.commons.querygen.Condition.HAS)
      .add(com.epam.reportportal.infrastructure.persistence.commons.querygen.Condition.ANY)
      .build();

  /**
   * Predicate that checks if filter condition should be applied with HAVING
   */
  public final static BiPredicate<FilterCondition, FilterTarget> HAVING_CONDITION = (filterCondition, target) -> {
    CriteriaHolder criteria = target.getCriteriaByFilter(filterCondition.getSearchCriteria())
        .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_FILTER_PARAMETERS,
            filterCondition.getSearchCriteria()));
    return HAVING_CONDITIONS.contains(filterCondition.getCondition())
        || filterCondition.getSearchCriteria().startsWith(STATISTICS_KEY)
        || !criteria.getQueryCriteria().equals(criteria.getAggregateCriteria());
  };

  /**
   * JOOQ SQL query representation
   */
  private QuerySupplier query;

  private FilterTarget filterTarget;

  private QueryBuilder(FilterTarget target) {
    filterTarget = target;
    query = target.getQuery();
  }

  private QueryBuilder(FilterTarget filterTarget, Set<String> fields) {
    this.filterTarget = filterTarget;
    this.query = this.filterTarget.getQuery();
    addJoinsToQuery(this.query, this.filterTarget, fields);
  }

  private QueryBuilder(Queryable query) {
    filterTarget = query.getTarget();
    this.query = query.toQuery();
  }

  private QueryBuilder(Queryable queryable, Set<String> fields) {
    filterTarget = queryable.getTarget();
    this.query = queryable.toQuery();
    addJoinsToQuery(this.query, filterTarget, fields);
  }

  public static QueryBuilder newBuilder(FilterTarget target) {
    return new QueryBuilder(target);
  }

  public static QueryBuilder newBuilder(FilterTarget target, Set<String> fields) {
    return new QueryBuilder(target, fields);
  }

  public static QueryBuilder newBuilder(Queryable queryable) {
    return new QueryBuilder(queryable);
  }

  public static QueryBuilder newBuilder(Queryable queryable, Set<String> fields) {
    return new QueryBuilder(queryable, fields);
  }

  public static int retrieveOffsetAndApplyBoundaries(Pageable pageable) {

    long offset = pageable.getOffset();

    if (offset < 0) {
      return 0;
    }

    if (offset > Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    } else {
      return (int) offset;
    }

  }

  public QueryBuilder addJointToStart(TableLike<? extends Record> table, JoinType joinType,
      Condition condition) {
    if (table != null && joinType != null && condition != null) {
      query.addJoinToStart(JoinEntity.of(table, joinType, condition));
    }
    return this;
  }

  public QueryBuilder addJoinToEnd(TableLike<? extends Record> table, JoinType joinType,
      Condition condition) {
    if (table != null && joinType != null && condition != null) {
      query.addJoinToEnd(JoinEntity.of(table, joinType, condition));
    }
    return this;
  }

  /**
   * Adds condition to the query
   *
   * @param condition Condition
   * @return QueryBuilder
   */
  public QueryBuilder addCondition(Condition condition) {
    if (null != condition) {
      query.addCondition(condition);
    }
    return this;
  }

  /**
   * Adds statistics condition to the query
   *
   * @param condition Condition
   */
  public QueryBuilder addHavingCondition(Condition condition) {
    if (null != condition) {
      query.addHaving(condition);
    }
    return this;
  }

  /**
   * Adds {@link Pageable} conditions
   *
   * @param pageable Pageable
   * @return QueryBuilder
   */
  public QueryBuilder with(Pageable pageable) {
    if (pageable.isPaged()) {
      query.addLimit(pageable.getPageSize());
      int offset = retrieveOffsetAndApplyBoundaries(pageable);
      query.addOffset(offset);
    }
    return with(pageable.getSort());
  }

  /**
   * Add limit
   *
   * @param limit Limit
   * @return QueryBuilder
   */
  public QueryBuilder with(int limit) {
    query.addLimit(limit);
    return this;
  }

  /**
   * Add offset
   *
   * @param offset offset
   * @return QueryBuilder
   */
  public QueryBuilder withOffset(int offset) {
    query.addOffset(offset);
    return this;
  }

  /**
   * Convert properties to query criteria and add sorting {@link Sort}
   *
   * @param sort Sort condition
   * @return QueryBuilder
   */
  public QueryBuilder with(Sort sort) {

    Set<Pair<String, Sort.Direction>> sortingSelect = Sets.newLinkedHashSet();

    ofNullable(sort).ifPresent(s -> s.get().forEach(order -> {
      CriteriaHolder criteria = filterTarget.getCriteriaByFilter(order.getProperty())
          .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_SORTING_PARAMETERS,
              order.getProperty()));
      Pair<String, Sort.Direction> sorting = Pair.of(criteria.getFilterCriteria(),
          order.getDirection());
      if (!order.getProperty().equalsIgnoreCase(CRITERIA_ID) && !sortingSelect.contains(sorting)) {
        query.addSelect(field(criteria.getAggregateCriteria()).as(criteria.getFilterCriteria()));
        sortingSelect.add(sorting);
      }
      Field<?> sortField = field(criteria.getAggregateCriteria());

      if (String.class.equals(criteria.getDataType())) {
        sortField = sortField.lower();
      }

      query.addOrderBy(
          sortField.sort(order.getDirection().isDescending() ? SortOrder.DESC : SortOrder.ASC));
    }));
    return this;
  }

  public QueryBuilder with(Field<?> field, SortOrder sort) {
    query.addOrderBy(field.sort(sort));
    return this;
  }

  public QuerySupplier getQuerySupplier() {
    return query;
  }

  /**
   * Builds query
   *
   * @return Query
   */
  public SelectQuery<? extends Record> build() {
    return query.get();
  }

  /**
   * Joins inner query to load all columns after filtering
   *
   * @return Query builder
   */
  public QueryBuilder wrap() {
    query = filterTarget.wrapQuery(query.get());
    return this;
  }

  /**
   * Joins inner query to load columns excluding provided fields after filtering
   *
   * @return Query builder
   */
  public QueryBuilder wrapExcludingFields(String... excludingFields) {
    query = filterTarget.wrapQuery(query.get(), excludingFields);
    return this;
  }

  public QueryBuilder withWrapperSort(Sort sort) {
    ofNullable(sort).ifPresent(s -> StreamSupport.stream(s.spliterator(), false).forEach(order -> {
      CriteriaHolder criteria = filterTarget.getCriteriaByFilter(order.getProperty())
          .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_SORTING_PARAMETERS,
              order.getProperty()));
      if (criteria.getFilterCriteria().startsWith(STATISTICS_KEY)) {
        if (filterTarget.withGrouping()) {
          query.addGroupBy(fieldName(FILTERED_QUERY, criteria.getFilterCriteria()));
        }

        query.addOrderBy(fieldName(FILTERED_QUERY, criteria.getFilterCriteria()).sort(
            order.getDirection().isDescending() ?
                SortOrder.DESC :
                SortOrder.ASC));
      } else {
        if (filterTarget.withGrouping()) {
          query.addGroupBy(field(criteria.getQueryCriteria()));
        }

        query.addOrderBy(
            field(criteria.getQueryCriteria()).sort(order.getDirection().isDescending() ?
                SortOrder.DESC :
                SortOrder.ASC));
      }
    }));
    return this;
  }

  private void addJoinsToQuery(QuerySupplier query, FilterTarget filterTarget, Set<String> fields) {
    Map<TableLike, JoinEntity> joinTables = new LinkedHashMap<>();
    fields.forEach(it -> {
      if (!joinTables.containsKey(STATISTICS) && it.startsWith(STATISTICS_KEY)) {
        ofNullable(STATISTICS_TARGET_MAPPING.get(filterTarget)).ifPresent(joinEntity -> {
          joinTables.put(STATISTICS, joinEntity);
          joinTables.put(STATISTICS_FIELD,
              JoinEntity.of(STATISTICS_FIELD,
                  JoinType.LEFT_OUTER_JOIN,
                  STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID)
              )
          );
        });
      } else {
        filterTarget.getCriteriaByFilter(it)
            .ifPresent(criteriaHolder -> criteriaHolder.getJoinChain().forEach(joinEntity -> {
              if (!joinTables.containsKey(joinEntity.getTable())) {
                joinTables.put(joinEntity.getTable(), joinEntity);
              }
            }));

      }
    });
    joinTables.forEach((key, value) -> query.addJoin(value.getTable(), value.getJoinType(),
        value.getJoinCondition()));
  }

  /**
   * Adds group by condition
   */
  public QueryBuilder addGroupByFields(Collection<? extends GroupField> fields) {
    if (CollectionUtils.isNotEmpty(fields)) {
      query.addGroupBy(fields);
    }

    return this;
  }
}
