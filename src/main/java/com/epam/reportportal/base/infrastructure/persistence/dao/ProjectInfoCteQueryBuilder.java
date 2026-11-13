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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder.HAVING_CONDITION;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_CREATION_DATE;
import static com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectInfo.LAST_RUN;
import static com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectInfo.LAUNCHES_QUANTITY;
import static com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectInfo.USERS_QUANTITY;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.LAUNCH;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.PROJECT;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.PROJECT_USER;
import static org.jooq.impl.DSL.asterisk;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.countDistinct;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.with;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.CriteriaHolder;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectInfo;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JStatusEnum;
import jakarta.annotation.Nullable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jooq.CommonTableExpression;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Operator;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.SortOrder;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * CTE-based query builder for PROJECT_INFO optimization.
 */
@Slf4j
@Component
public class ProjectInfoCteQueryBuilder {

  private static final String CTE_BASE_PROJECTS = "base_projects";
  private static final String CTE_PROJECT_USERS = "pu";
  private static final String CTE_LAUNCHES = "la";
  private static final String CTE_FILTERED = "filtered";
  private static final String ALIAS_FILTERED = "f";
  private static final String FIELD_TOTAL_COUNT = "total_count";

  private final DSLContext dsl;

  public ProjectInfoCteQueryBuilder(DSLContext dsl) {
    this.dsl = dsl;
  }

  /**
   * Builds paginated result using CTE query with window function.
   */
  public Page<ProjectInfo> findPagedProjectInfo(Queryable filter, Pageable pageable) {
    List<Record> records = dsl.fetch(buildQuery(filter, pageable));

    if (records.isEmpty()) {
      return Page.empty(pageable);
    }

    long totalCount = records.get(0).get(FIELD_TOTAL_COUNT, Long.class);
    List<ProjectInfo> content = records.stream()
        .map(r -> r.into(ProjectInfo.class))
        .toList();

    return new PageImpl<>(content, pageable, totalCount);
  }

  /**
   * Finds list of project info without pagination.
   */
  public List<ProjectInfo> findProjectInfo(Queryable filter) {
    return dsl.fetch(buildQuery(filter, Pageable.unpaged()))
        .into(ProjectInfo.class);
  }

  private SelectQuery<Record> buildQuery(Queryable filter, Pageable pageable) {
    List<FilterCondition> baseFilters = new ArrayList<>();
    List<FilterCondition> aggregateFilters = new ArrayList<>();
    separateFilters(filter, baseFilters, aggregateFilters);

    var baseProjectsCte = createBaseProjectsCte(baseFilters, filter);
    var usersCte = createUserAggregationCte();
    var launchesCte = createLaunchAggregationCte();
    var filteredCte = createFilteredCte(aggregateFilters, filter);

    SelectQuery<Record> query = with(baseProjectsCte, usersCte, launchesCte, filteredCte)
        .select(asterisk(), count().over().as(FIELD_TOTAL_COUNT))
        .from(table(name(CTE_FILTERED)).as(ALIAS_FILTERED))
        .getQuery();

    applySorting(query, pageable);
    applyPagination(query, pageable);

    return query;
  }

  private void separateFilters(Queryable queryable, List<FilterCondition> baseFilters,
      List<FilterCondition> aggregateFilters) {
    for (ConvertibleCondition condition : queryable.getFilterConditions()) {
      for (FilterCondition fc : condition.getAllConditions()) {
        if (HAVING_CONDITION.test(fc, queryable.getTarget())) {
          aggregateFilters.add(fc);
        } else {
          baseFilters.add(fc);
        }
      }
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private CommonTableExpression<Record> createBaseProjectsCte(List<FilterCondition> filters,
      Queryable queryable) {
    SelectQuery query = select(
        PROJECT.ID,
        PROJECT.CREATED_AT,
        PROJECT.NAME,
        PROJECT.KEY,
        PROJECT.SLUG,
        PROJECT.ORGANIZATION,
        PROJECT.ORGANIZATION_ID
    ).from(PROJECT).getQuery();

    applyConditions(query, filters, queryable, false);
    return name(CTE_BASE_PROJECTS).as((Select<Record>) query);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private CommonTableExpression<Record> createUserAggregationCte() {
    Select cte = select(
        PROJECT_USER.PROJECT_ID,
        countDistinct(PROJECT_USER.USER_ID).as(USERS_QUANTITY)
    )
        .from(PROJECT_USER)
        .join(table(name(CTE_BASE_PROJECTS)))
        .on(PROJECT_USER.PROJECT_ID.eq(field(name(CTE_BASE_PROJECTS, PROJECT.ID.getName()), Long.class)))
        .groupBy(PROJECT_USER.PROJECT_ID);
    return name(CTE_PROJECT_USERS).as((Select<Record>) cte);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private CommonTableExpression<Record> createLaunchAggregationCte() {
    Condition finishedLaunchFilter = LAUNCH.STATUS.ne(JStatusEnum.IN_PROGRESS);

    Select cte = select(
        LAUNCH.PROJECT_ID,
        count().filterWhere(finishedLaunchFilter).as(LAUNCHES_QUANTITY),
        max(LAUNCH.START_TIME).as(LAST_RUN)
    )
        .from(LAUNCH)
        .join(table(name(CTE_BASE_PROJECTS)))
        .on(LAUNCH.PROJECT_ID.eq(field(name(CTE_BASE_PROJECTS, PROJECT.ID.getName()), Long.class)))
        .groupBy(LAUNCH.PROJECT_ID);
    return name(CTE_LAUNCHES).as((Select<Record>) cte);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private CommonTableExpression<Record> createFilteredCte(List<FilterCondition> filters,
      Queryable queryable) {
    String bpAlias = "bp";
    Field<Long> bpId = field(name(bpAlias, PROJECT.ID.getName()), Long.class);

    SelectQuery query = select(
        bpId.as(PROJECT.ID.getName()),
        field(name(bpAlias, PROJECT.CREATED_AT.getName())).as(PROJECT.CREATED_AT.getName()),
        field(name(bpAlias, PROJECT.NAME.getName())),
        field(name(bpAlias, PROJECT.KEY.getName())),
        field(name(bpAlias, PROJECT.SLUG.getName())),
        field(name(bpAlias, PROJECT.ORGANIZATION.getName())),
        field(name(bpAlias, PROJECT.ORGANIZATION_ID.getName())),
        coalesce(field(name(CTE_PROJECT_USERS, USERS_QUANTITY), Long.class), inline(0L)).as(USERS_QUANTITY),
        coalesce(field(name(CTE_LAUNCHES, LAUNCHES_QUANTITY), Long.class), inline(0L)).as(LAUNCHES_QUANTITY),
        field(name(CTE_LAUNCHES, LAST_RUN)).as(LAST_RUN)
    )
        .from(table(name(CTE_BASE_PROJECTS)).as(bpAlias))
        .leftJoin(table(name(CTE_PROJECT_USERS)))
        .on(bpId.eq(field(name(CTE_PROJECT_USERS, PROJECT_USER.PROJECT_ID.getName()), Long.class)))
        .leftJoin(table(name(CTE_LAUNCHES)))
        .on(bpId.eq(field(name(CTE_LAUNCHES, LAUNCH.PROJECT_ID.getName()), Long.class)))
        .getQuery();

    applyConditions(query, filters, queryable, true);
    return name(CTE_FILTERED).as((Select<Record>) query);
  }

  private void applyConditions(SelectQuery<Record> query, List<FilterCondition> filters, Queryable queryable,
      boolean aggregateContext) {
    if (filters.isEmpty()) {
      return;
    }

    List<Condition> andConditions = new ArrayList<>();
    List<Condition> orConditions = new ArrayList<>();

    for (FilterCondition fc : filters) {
      var criteriaHolderOpt = queryable.getTarget().getCriteriaByFilter(fc.getSearchCriteria());
      if (criteriaHolderOpt.isEmpty()) {
        continue;
      }

      var criteriaHolder = criteriaHolderOpt.get();
      Condition condition = buildConditionForCte(fc, criteriaHolder, aggregateContext);

      if (condition != null) {
        if (fc.getOperator() == Operator.OR) {
          orConditions.add(condition);
        } else {
          andConditions.add(condition);
        }
      }
    }

    // Add OR conditions as a single combined condition
    if (!orConditions.isEmpty()) {
      Condition combinedOr = orConditions.stream()
          .reduce(DSL::or)
          .orElse(DSL.noCondition());
      query.addConditions(combinedOr);
    }

    // Add AND conditions individually (they're combined with AND by default)
    andConditions.forEach(query::addConditions);
  }

  private Condition buildConditionForCte(FilterCondition fc, CriteriaHolder criteriaHolder,
      boolean aggregateContext) {
    String mappedFieldName = mapFilterField(fc.getSearchCriteria());
    Field<Object> field = aggregateContext ? resolveAggregateField(mappedFieldName) : field(name(mappedFieldName));
    String value = fc.getValue();

    try {
      Condition condition = switch (fc.getCondition()) {
        case EQUALS -> field.eq(criteriaHolder.castValue(value));
        case NOT_EQUALS -> field.ne(criteriaHolder.castValue(value));
        case CONTAINS -> field.likeIgnoreCase("%" + DSL.escape(value, '\\') + "%");
        case IN -> {
          String[] values = value.split(",");
          Object[] casted = new Object[values.length];
          for (int i = 0; i < values.length; i++) {
            casted[i] = criteriaHolder.castValue(values[i].trim());
          }
          yield field.in(casted);
        }
        case GREATER_THAN -> field.gt(criteriaHolder.castValue(value));
        case GREATER_THAN_OR_EQUALS -> field.ge(criteriaHolder.castValue(value));
        case LOWER_THAN -> field.lt(criteriaHolder.castValue(value));
        case LOWER_THAN_OR_EQUALS -> field.le(criteriaHolder.castValue(value));
        case BETWEEN -> {
          // Handle special timestamp format: "minutes1;minutes2;timezone"
          if (value.contains(";")) {
            String[] parts = value.split(";");
            if (parts.length == 3) {
              yield getCondition(criteriaHolder, parts, field);
            }
          }

          // Standard format: "value1,value2"
          String[] parts = value.split(",");
          if (parts.length == 2) {
            yield field.between(
                criteriaHolder.castValue(parts[0].trim()),
                criteriaHolder.castValue(parts[1].trim())
            );
          }
          yield null;
        }
        default -> null;
      };

      return condition != null && fc.isNegative() ? condition.not() : condition;
    } catch (Exception e) {
      log.warn("Invalid conditions", e);
      return null;
    }
  }

  // Must mirror the SELECT list expression: an unqualified name would otherwise resolve to the raw,
  // possibly-NULL column of the left-joined aggregation CTE, so "= 0" would never match.
  private Field<Object> resolveAggregateField(String mappedFieldName) {
    if (USERS_QUANTITY.equals(mappedFieldName)) {
      return coalesce(field(name(CTE_PROJECT_USERS, USERS_QUANTITY), Long.class), inline(0L)).coerce(Object.class);
    }
    if (LAUNCHES_QUANTITY.equals(mappedFieldName)) {
      return coalesce(field(name(CTE_LAUNCHES, LAUNCHES_QUANTITY), Long.class), inline(0L)).coerce(Object.class);
    }
    return field(name(mappedFieldName));
  }

  @Nullable
  private static Condition getCondition(CriteriaHolder criteriaHolder, String[] parts, Field<Object> field) {
    try {
      ZoneOffset offset = ZoneOffset.of(parts[2]);
      ZonedDateTime localDateTime = ZonedDateTime.now(offset).toLocalDate().atStartOfDay(offset);
      long start = java.util.Date.from(localDateTime.plusMinutes(Long.parseLong(parts[0])).toInstant()).getTime();
      long end = java.util.Date.from(localDateTime.plusMinutes(Long.parseLong(parts[1])).toInstant()).getTime();

      Object startValue = criteriaHolder.castValue(String.valueOf(start));
      Object endValue = criteriaHolder.castValue(String.valueOf(end));
      return field.between(startValue, endValue);
    } catch (Exception e) {
      return null;
    }
  }

  private void applySorting(SelectQuery<Record> query, Pageable pageable) {
    if (pageable.getSort().isSorted()) {
      for (var order : pageable.getSort()) {
        String mappedFieldName = mapSortField(order.getProperty());
        query.addOrderBy(field(name(ALIAS_FILTERED, mappedFieldName))
            .sort(order.isAscending() ? SortOrder.ASC : SortOrder.DESC));
      }
    }
  }

  private String mapSortField(String apiFieldName) {
    return switch (apiFieldName) {
      case CRITERIA_PROJECT_CREATION_DATE -> PROJECT.CREATED_AT.getName();
      case USERS_QUANTITY -> USERS_QUANTITY;
      case LAUNCHES_QUANTITY -> LAUNCHES_QUANTITY;
      case LAST_RUN -> LAST_RUN;
      default -> apiFieldName;
    };
  }

  private String mapFilterField(String apiFieldName) {
    return switch (apiFieldName) {
      case CRITERIA_PROJECT_CREATION_DATE -> PROJECT.CREATED_AT.getName();
      case USERS_QUANTITY -> USERS_QUANTITY;
      case LAUNCHES_QUANTITY -> LAUNCHES_QUANTITY;
      case LAST_RUN -> LAST_RUN;
      default -> apiFieldName;
    };
  }

  private void applyPagination(SelectQuery<Record> query, Pageable pageable) {
    if (pageable.isPaged()) {
      query.addLimit(pageable.getPageSize());
      query.addOffset((int) pageable.getOffset());
    }
  }
}
