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

package com.epam.reportportal.infrastructure.persistence.dao;

import static com.epam.reportportal.infrastructure.persistence.commons.querygen.Condition.VALUES_SEPARATOR;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.QueryBuilder.STATISTICS_KEY;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_START_TIME;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.ItemAttributeConstant.KEY_VALUE_SEPARATOR;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_DURATION;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.AGGREGATED_LAUNCHES_IDS;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ATTRIBUTE_KEY;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ATTRIBUTE_VALUE;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ATTR_ID;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ATTR_TABLE;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.CRITERIA;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.CRITERIA_FLAG;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.CRITERIA_TABLE;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.CUSTOM_ATTRIBUTE;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.CUSTOM_COLUMN;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.DEFECTS_AUTOMATION_BUG_TOTAL;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.DEFECTS_KEY;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.DEFECTS_NO_DEFECT_TOTAL;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.DEFECTS_PRODUCT_BUG_TOTAL;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.DEFECTS_SYSTEM_ISSUE_TOTAL;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.DEFECTS_TO_INVESTIGATE_TOTAL;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.DELTA;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.DURATION;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_FAILED;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_KEY;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_PASSED;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_SKIPPED;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_TOTAL;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.FILTER_NAME;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.FIRST_LEVEL_ID;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.FLAKY_CASES_LIMIT;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.FLAKY_COUNT;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.FLAKY_TABLE_RESULTS;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.INVESTIGATED;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ITEMS;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ITEM_ATTRIBUTES;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ITEM_ID;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ITEM_NAME;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.KEY;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.LAUNCHES;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.LAUNCH_ID;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.MOST_FAILED_CRITERIA_LIMIT;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.NAME;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.NUMBER;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.PASSED;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.PASSING_RATE;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.PATTERNS_COUNT;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.PERCENTAGE;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.PERCENTAGE_MULTIPLIER;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.SF_NAME;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.SKIPPED;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.START_TIME;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.START_TIME_HISTORY;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.STATISTICS_COUNTER;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.STATISTICS_SEPARATOR;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.STATISTICS_TABLE;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.STATUS;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.STATUSES;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.STATUS_HISTORY;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.SUM;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.SWITCH_FLAG;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.TOTAL;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.TO_INVESTIGATE;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.UNIQUE_ID;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.VALUE;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.VERSION_DELIMITER;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.VERSION_PATTERN;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ZERO_QUERY_VALUE;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetRepositoryConstants.ID;
import static com.epam.reportportal.infrastructure.persistence.dao.util.JooqFieldNameTransformer.fieldName;
import static com.epam.reportportal.infrastructure.persistence.dao.util.QueryUtils.collectJoinFields;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.ACTIVITY_MAPPER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.BUG_TREND_STATISTICS_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.CASES_GROWTH_TREND_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.COMPONENT_HEALTH_CHECK_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.CRITERIA_HISTORY_ITEM_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.CUMULATIVE_TOOLTIP_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.CUMULATIVE_TREND_CHART_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.FLAKY_CASES_TABLE_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.INVESTIGATED_STATISTICS_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.LAUNCHES_STATISTICS_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.LAUNCHES_TABLE_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.NOT_PASSED_CASES_CONTENT_RECORD_MAPPER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.OVERALL_STATISTICS_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.PATTERN_TEMPLATES_AGGREGATION_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.PRODUCT_STATUS_FILTER_GROUPED_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.PRODUCT_STATUS_LAUNCH_GROUPED_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.TIMELINE_INVESTIGATED_STATISTICS_RECORD_MAPPER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.TOP_PATTERN_TEMPLATES_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.TOP_PATTERN_TEMPLATES_GROUPED_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.WidgetContentUtil.UNIQUE_BUG_CONTENT_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.FILTER;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.ITEM_ATTRIBUTE;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.PATTERN_TEMPLATE;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.PATTERN_TEMPLATE_TEST_ITEM;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.STATISTICS;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.STATISTICS_FIELD;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JIssue.ISSUE;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JIssueTicket.ISSUE_TICKET;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JLaunch.LAUNCH;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JTestItem.TEST_ITEM;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JTestItemResults.TEST_ITEM_RESULTS;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JTicket.TICKET;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JUsers.USERS;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toList;
import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.arrayAggDistinct;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.concat;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.lag;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.nullif;
import static org.jooq.impl.DSL.orderBy;
import static org.jooq.impl.DSL.round;
import static org.jooq.impl.DSL.selectDistinct;
import static org.jooq.impl.DSL.sum;
import static org.jooq.impl.DSL.timestampDiff;
import static org.jooq.impl.DSL.val;
import static org.jooq.impl.DSL.when;

import com.epam.reportportal.infrastructure.model.ActivityResource;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.CriteriaHolder;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.QueryBuilder;
import com.epam.reportportal.infrastructure.persistence.dao.util.QueryUtils;
import com.epam.reportportal.infrastructure.persistence.dao.widget.WidgetProviderChain;
import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.ChartStatisticsContent;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.CriteriaHistoryItem;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.CumulativeTrendChartEntry;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.FlakyCasesTableContent;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.LaunchesDurationContent;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.LaunchesTableContent;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.MostTimeConsumingTestCasesContent;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.NotPassedCasesContent;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.OverallStatisticsContent;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.PassingRateStatisticsResult;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.ProductStatusStatisticsContent;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.TopPatternTemplatesContent;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.UniqueBugContent;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.healthcheck.ComponentHealthCheckContent;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.healthcheck.HealthCheckTableContent;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.healthcheck.HealthCheckTableGetParams;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.healthcheck.HealthCheckTableInitParams;
import com.epam.reportportal.infrastructure.persistence.jooq.enums.JStatusEnum;
import com.epam.reportportal.infrastructure.persistence.jooq.enums.JTestItemTypeEnum;
import com.epam.reportportal.infrastructure.persistence.jooq.tables.JItemAttribute;
import com.epam.reportportal.infrastructure.persistence.util.WidgetSortUtils;
import com.epam.reportportal.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.google.common.collect.Lists;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Operator;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record4;
import org.jooq.Record5;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectHavingStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.SelectQuery;
import org.jooq.SelectSeekStepN;
import org.jooq.SortOrder;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/**
 * Repository that contains queries of content loading for widgets.
 *
 * @author Pavel Bortnik
 */
@Repository
public class WidgetContentRepositoryImpl implements WidgetContentRepository {

  private static final List<JTestItemTypeEnum> HAS_METHOD_OR_CLASS = Arrays.stream(
      JTestItemTypeEnum.values()).filter(it -> {
    String name = it.name();
    return name.contains("METHOD") || name.contains("CLASS");
  }).collect(Collectors.toList());
  @Autowired
  private DSLContext dsl;
  @Autowired
  private WidgetProviderChain<HealthCheckTableGetParams, List<HealthCheckTableContent>> healthCheckTableChain;

  @Override
  public OverallStatisticsContent overallStatisticsContent(Filter filter, Sort sort,
      List<String> contentFields, boolean latest,
      int limit) {

    return OVERALL_STATISTICS_FETCHER.apply(dsl.with(LAUNCHES)
        .as(QueryUtils.createQueryBuilderWithLatestLaunchesOption(filter, sort, latest).with(sort)
            .with(limit).build())
        .select(STATISTICS_FIELD.NAME, sum(STATISTICS.S_COUNTER).as(SUM))
        .from(LAUNCH)
        .join(LAUNCHES)
        .on(LAUNCH.ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
        .leftJoin(STATISTICS)
        .on(LAUNCH.ID.eq(STATISTICS.LAUNCH_ID))
        .join(STATISTICS_FIELD)
        .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
        .where(STATISTICS_FIELD.NAME.in(contentFields))
        .groupBy(STATISTICS_FIELD.NAME)
        .fetch());
  }

  /**
   * Returns condition for step level test item types. Include before/after methods and classes types depends on
   * {@code includeMethods} param.
   *
   * @param includeMethods
   * @return {@link Condition}
   */
  private Condition itemTypeStepCondition(boolean includeMethods) {
    List<JTestItemTypeEnum> itemTypes = Lists.newArrayList(JTestItemTypeEnum.STEP);
    if (includeMethods) {
      itemTypes.addAll(HAS_METHOD_OR_CLASS);
    }
    return TEST_ITEM.TYPE.in(itemTypes);
  }

  @Override
  public List<CriteriaHistoryItem> topItemsByCriteria(Filter filter, String criteria, int limit,
      boolean includeMethods) {
    Table<Record2<Long, BigDecimal>> criteriaTable = getTopItemsCriteriaTable(filter, criteria,
        limit, includeMethods);

    return CRITERIA_HISTORY_ITEM_FETCHER.apply(dsl.select(TEST_ITEM.UNIQUE_ID,
            TEST_ITEM.NAME,
            DSL.arrayAgg(
                    when(fieldName(criteriaTable.getName(), CRITERIA_FLAG).cast(Integer.class).ge(1),
                        true).otherwise(false))
                .orderBy(LAUNCH.NUMBER.asc())
                .as(STATUS_HISTORY),
            DSL.max(TEST_ITEM.LAUNCH_ID).as(TEST_ITEM.LAUNCH_ID.getName()),
            DSL.max(TEST_ITEM.START_TIME).filterWhere(
                    fieldName(criteriaTable.getName(), CRITERIA_FLAG).cast(Integer.class).ge(1))
                .as(START_TIME_HISTORY),
            DSL.sum(fieldName(criteriaTable.getName(), CRITERIA_FLAG).cast(Integer.class)).as(CRITERIA),
            DSL.count(TEST_ITEM.ITEM_ID).as(TOTAL)
        )
        .from(TEST_ITEM)
        .join(criteriaTable)
        .on(TEST_ITEM.ITEM_ID.eq(fieldName(criteriaTable.getName(), ITEM_ID).cast(Long.class)))
        .join(LAUNCH)
        .on(TEST_ITEM.LAUNCH_ID.eq(LAUNCH.ID))
        .groupBy(TEST_ITEM.UNIQUE_ID, TEST_ITEM.NAME)
        .having(DSL.sum(fieldName(criteriaTable.getName(), CRITERIA_FLAG).cast(Integer.class))
            .greaterThan(BigDecimal.ZERO))
        .orderBy(DSL.field(name(CRITERIA)).desc(), DSL.field(name(TOTAL)).asc())
        .limit(MOST_FAILED_CRITERIA_LIMIT)
        .fetch());
  }

  private Table<Record2<Long, BigDecimal>> getTopItemsCriteriaTable(Filter filter, String criteria,
      int limit, boolean includeMethods) {
    Sort launchSort = Sort.by(Sort.Direction.DESC, CRITERIA_START_TIME);
    Table<? extends Record> launchesTable = QueryBuilder.newBuilder(filter,
            collectJoinFields(filter, launchSort))
        .with(limit)
        .with(launchSort)
        .build()
        .asTable(LAUNCHES);

    return getCommonMostFailedQuery(criteria, launchesTable).where(
            itemTypeStepCondition(includeMethods))
        .and(TEST_ITEM.HAS_STATS.eq(Boolean.TRUE))
        .and(TEST_ITEM.HAS_CHILDREN.eq(false))
        .groupBy(TEST_ITEM.ITEM_ID)
        .asTable(CRITERIA_TABLE);
  }

  private SelectOnConditionStep<Record2<Long, BigDecimal>> getCommonMostFailedQuery(String criteria,
      Table<? extends Record> launchesTable) {
    if (StringUtils.endsWithAny(criteria,
        StatusEnum.FAILED.getExecutionCounterField(),
        StatusEnum.SKIPPED.getExecutionCounterField()
    )) {
      StatusEnum status = StatusEnum.fromValue(
              StringUtils.substringAfterLast(criteria, STATISTICS_SEPARATOR))
          .orElseThrow(() -> new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR));
      return statusCriteriaTable(JStatusEnum.valueOf(status.name()), launchesTable);
    } else {
      return statisticsCriteriaTable(criteria, launchesTable);
    }
  }

  private SelectOnConditionStep<Record2<Long, BigDecimal>> statisticsCriteriaTable(String criteria,
      Table<? extends Record> launchesTable) {
    return dsl.select(TEST_ITEM.ITEM_ID,
            sum(when(STATISTICS_FIELD.NAME.eq(criteria), 1).otherwise(ZERO_QUERY_VALUE)).as(
                CRITERIA_FLAG))
        .from(TEST_ITEM)
        .join(launchesTable)
        .on(TEST_ITEM.LAUNCH_ID.eq(fieldName(launchesTable.getName(), ID).cast(Long.class)))
        .join(STATISTICS)
        .on(TEST_ITEM.ITEM_ID.eq(STATISTICS.ITEM_ID))
        .join(STATISTICS_FIELD)
        .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID));
  }

  private SelectOnConditionStep<Record2<Long, BigDecimal>> statusCriteriaTable(JStatusEnum criteria,
      Table<? extends Record> launchesTable) {
    return dsl.select(TEST_ITEM.ITEM_ID,
            sum(when(TEST_ITEM_RESULTS.STATUS.eq(criteria), 1).otherwise(ZERO_QUERY_VALUE)).as(
                CRITERIA_FLAG)
        )
        .from(TEST_ITEM)
        .join(launchesTable)
        .on(TEST_ITEM.LAUNCH_ID.eq(fieldName(launchesTable.getName(), ID).cast(Long.class)))
        .join(TEST_ITEM_RESULTS)
        .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID));
  }

  @Override
  public List<FlakyCasesTableContent> flakyCasesStatistics(Filter filter, boolean includeMethods,
      int limit) {

    return FLAKY_CASES_TABLE_FETCHER.apply(
        dsl.select(field(name(FLAKY_TABLE_RESULTS, TEST_ITEM.UNIQUE_ID.getName())).as(UNIQUE_ID),
                field(name(FLAKY_TABLE_RESULTS, TEST_ITEM.NAME.getName())).as(ITEM_NAME),
                DSL.arrayAgg(field(name(FLAKY_TABLE_RESULTS, TEST_ITEM_RESULTS.STATUS.getName())))
                    .as(STATUSES),
                DSL.max(field(name(FLAKY_TABLE_RESULTS, START_TIME)))
                    .filterWhere(field(name(FLAKY_TABLE_RESULTS, SWITCH_FLAG)).cast(Integer.class)
                        .gt(ZERO_QUERY_VALUE))
                    .as(START_TIME_HISTORY),
                sum(field(name(FLAKY_TABLE_RESULTS, SWITCH_FLAG)).cast(Long.class)).as(FLAKY_COUNT),
                count(field(name(FLAKY_TABLE_RESULTS, ITEM_ID))).minus(1).as(TOTAL)
            )
            .from(dsl.with(LAUNCHES)
                .as(QueryBuilder.newBuilder(filter, collectJoinFields(filter, Sort.unsorted()))
                    .with(LAUNCH.NUMBER, SortOrder.DESC)
                    .with(limit)
                    .build())
                .select(TEST_ITEM.ITEM_ID,
                    TEST_ITEM.UNIQUE_ID,
                    TEST_ITEM.NAME,
                    TEST_ITEM.START_TIME,
                    TEST_ITEM_RESULTS.STATUS,
                    when(TEST_ITEM_RESULTS.STATUS.notEqual(
                            lag(TEST_ITEM_RESULTS.STATUS).over(orderBy(TEST_ITEM.UNIQUE_ID,
                                TEST_ITEM.START_TIME
                            )))
                        .and(TEST_ITEM.UNIQUE_ID.equal(
                            lag(TEST_ITEM.UNIQUE_ID).over(orderBy(TEST_ITEM.UNIQUE_ID,
                                TEST_ITEM.START_TIME
                            )))), 1).otherwise(ZERO_QUERY_VALUE).as(SWITCH_FLAG)
                )
                .from(LAUNCH)
                .join(LAUNCHES)
                .on(LAUNCH.ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
                .join(TEST_ITEM)
                .on(LAUNCH.ID.eq(TEST_ITEM.LAUNCH_ID))
                .join(TEST_ITEM_RESULTS)
                .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
                .where(itemTypeStepCondition(includeMethods))
                .and(TEST_ITEM.HAS_STATS.eq(Boolean.TRUE))
                .and(TEST_ITEM.HAS_CHILDREN.eq(false))
                .and(TEST_ITEM.RETRY_OF.isNull())
                .and(TEST_ITEM_RESULTS.STATUS.notEqual(JStatusEnum.SKIPPED))
                .groupBy(TEST_ITEM.ITEM_ID, TEST_ITEM_RESULTS.STATUS, TEST_ITEM.UNIQUE_ID,
                    TEST_ITEM.NAME, TEST_ITEM.START_TIME)
                .orderBy(TEST_ITEM.UNIQUE_ID, TEST_ITEM.START_TIME.desc())
                .asTable(FLAKY_TABLE_RESULTS))
            .groupBy(field(name(FLAKY_TABLE_RESULTS, TEST_ITEM.UNIQUE_ID.getName())),
                field(name(FLAKY_TABLE_RESULTS, TEST_ITEM.NAME.getName()))
            )
            .having(count(field(name(FLAKY_TABLE_RESULTS, ITEM_ID))).gt(BigDecimal.ONE.intValue())
                .and(sum(field(name(FLAKY_TABLE_RESULTS, SWITCH_FLAG)).cast(Long.class)).gt(
                    BigDecimal.ZERO)))
            .orderBy(fieldName(FLAKY_COUNT).desc(), fieldName(TOTAL).asc(), fieldName(UNIQUE_ID))
            .limit(FLAKY_CASES_LIMIT)
            .fetch());
  }

  @Override
  public List<ChartStatisticsContent> launchStatistics(Filter filter, List<String> contentFields,
      Sort sort, int limit) {

    List<Field<?>> groupingFields = Lists.newArrayList(field(LAUNCH.ID),
        field(LAUNCH.NUMBER),
        field(LAUNCH.START_TIME),
        field(LAUNCH.NAME),
        fieldName(STATISTICS_TABLE, SF_NAME),
        fieldName(STATISTICS_TABLE, STATISTICS_COUNTER)
    );

    groupingFields.addAll(
        WidgetSortUtils.fieldTransformer(filter.getTarget()).apply(sort, LAUNCHES));

    return LAUNCHES_STATISTICS_FETCHER.apply(dsl.with(LAUNCHES)
        .as(QueryBuilder.newBuilder(filter, collectJoinFields(filter, sort)).with(sort).with(limit)
            .build())
        .select(LAUNCH.ID,
            LAUNCH.NUMBER,
            LAUNCH.START_TIME,
            LAUNCH.NAME,
            fieldName(STATISTICS_TABLE, SF_NAME),
            fieldName(STATISTICS_TABLE, STATISTICS_COUNTER)
        )
        .from(LAUNCH)
        .join(LAUNCHES)
        .on(LAUNCH.ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
        .leftJoin(dsl.select(STATISTICS.LAUNCH_ID, STATISTICS.S_COUNTER.as(STATISTICS_COUNTER),
                STATISTICS_FIELD.NAME.as(SF_NAME))
            .from(STATISTICS)
            .join(STATISTICS_FIELD)
            .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
            .where(STATISTICS_FIELD.NAME.in(contentFields))
            .asTable(STATISTICS_TABLE))
        .on(LAUNCH.ID.eq(fieldName(STATISTICS_TABLE, LAUNCH_ID).cast(Long.class)))
        .groupBy(groupingFields)
        .orderBy(WidgetSortUtils.sortingTransformer(filter.getTarget()).apply(sort, LAUNCHES))
        .fetch());
  }

  @Override
  public List<ChartStatisticsContent> investigatedStatistics(Filter filter, Sort sort, int limit) {

    List<Field<?>> groupingFields = Lists.newArrayList(field(LAUNCH.ID),
        field(LAUNCH.NUMBER),
        field(LAUNCH.START_TIME),
        field(LAUNCH.NAME)
    );

    groupingFields.addAll(
        WidgetSortUtils.fieldTransformer(filter.getTarget()).apply(sort, LAUNCHES));

    return INVESTIGATED_STATISTICS_FETCHER.apply(dsl.with(LAUNCHES)
        .as(QueryBuilder.newBuilder(filter, collectJoinFields(filter, sort)).with(sort).with(limit)
            .build())
        .select(LAUNCH.ID,
            LAUNCH.NUMBER,
            LAUNCH.START_TIME,
            LAUNCH.NAME,
            round(val(PERCENTAGE_MULTIPLIER).mul(dsl.select(sum(STATISTICS.S_COUNTER))
                    .from(STATISTICS)
                    .join(STATISTICS_FIELD)
                    .onKey()
                    .where(STATISTICS_FIELD.NAME.eq(DEFECTS_TO_INVESTIGATE_TOTAL)
                        .and(STATISTICS.LAUNCH_ID.eq(LAUNCH.ID)))
                    .asField()
                    .cast(Double.class))
                .div(nullif(dsl.select(sum(STATISTICS.S_COUNTER))
                    .from(STATISTICS)
                    .join(STATISTICS_FIELD)
                    .onKey()
                    .where(STATISTICS_FIELD.NAME.in(DEFECTS_AUTOMATION_BUG_TOTAL,
                        DEFECTS_NO_DEFECT_TOTAL,
                        DEFECTS_TO_INVESTIGATE_TOTAL,
                        DEFECTS_PRODUCT_BUG_TOTAL,
                        DEFECTS_SYSTEM_ISSUE_TOTAL
                    ).and(STATISTICS.LAUNCH_ID.eq(LAUNCH.ID)))
                    .asField(), 0)), 2).as(TO_INVESTIGATE)
        )
        .from(LAUNCH)
        .join(LAUNCHES)
        .on(LAUNCH.ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
        .leftJoin(dsl.select(STATISTICS.LAUNCH_ID, STATISTICS.S_COUNTER.as(STATISTICS_COUNTER),
                STATISTICS_FIELD.NAME.as(SF_NAME))
            .from(STATISTICS)
            .join(STATISTICS_FIELD)
            .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
            .asTable(STATISTICS_TABLE))
        .on(LAUNCH.ID.eq(fieldName(STATISTICS_TABLE, LAUNCH_ID).cast(Long.class)))
        .groupBy(groupingFields)
        .orderBy(WidgetSortUtils.sortingTransformer(filter.getTarget()).apply(sort, LAUNCHES))
        .fetch());

  }

  @Override
  public List<ChartStatisticsContent> timelineInvestigatedStatistics(Filter filter, Sort sort,
      int limit) {

    List<Field<?>> groupingFields = Lists.newArrayList(field(LAUNCH.ID),
        field(LAUNCH.NUMBER),
        field(LAUNCH.START_TIME),
        field(LAUNCH.NAME)
    );

    groupingFields.addAll(
        WidgetSortUtils.fieldTransformer(filter.getTarget()).apply(sort, LAUNCHES));

    return dsl.with(LAUNCHES)
        .as(QueryBuilder.newBuilder(filter, collectJoinFields(filter, sort)).with(sort).with(limit)
            .build())
        .select(LAUNCH.ID,
            LAUNCH.NUMBER,
            LAUNCH.START_TIME,
            LAUNCH.NAME,
            coalesce(dsl.select(sum(STATISTICS.S_COUNTER))
                .from(STATISTICS)
                .join(STATISTICS_FIELD)
                .onKey()
                .where(STATISTICS_FIELD.NAME.eq(DEFECTS_TO_INVESTIGATE_TOTAL)
                    .and(STATISTICS.LAUNCH_ID.eq(LAUNCH.ID)))
                .asField()
                .cast(Double.class), 0).as(TO_INVESTIGATE),
            coalesce(dsl.select(sum(STATISTICS.S_COUNTER))
                .from(STATISTICS)
                .join(STATISTICS_FIELD)
                .onKey()
                .where(STATISTICS_FIELD.NAME.in(DEFECTS_AUTOMATION_BUG_TOTAL,
                    DEFECTS_NO_DEFECT_TOTAL,
                    DEFECTS_TO_INVESTIGATE_TOTAL,
                    DEFECTS_PRODUCT_BUG_TOTAL,
                    DEFECTS_SYSTEM_ISSUE_TOTAL
                ).and(STATISTICS.LAUNCH_ID.eq(LAUNCH.ID)))
                .asField(), 0).as(INVESTIGATED)
        )
        .from(LAUNCH)
        .join(LAUNCHES)
        .on(LAUNCH.ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
        .groupBy(groupingFields)
        .orderBy(WidgetSortUtils.sortingTransformer(filter.getTarget()).apply(sort, LAUNCHES))
        .fetch(TIMELINE_INVESTIGATED_STATISTICS_RECORD_MAPPER);
  }

  @Override
  public PassingRateStatisticsResult passingRatePerLaunchStatistics(Long launchId) {

    return dsl.select(LAUNCH.ID,
            LAUNCH.NUMBER.as(NUMBER),
            sum(when(STATISTICS_FIELD.NAME.eq(EXECUTIONS_PASSED), STATISTICS.S_COUNTER).otherwise(
                0)).as(PASSED),
            sum(when(STATISTICS_FIELD.NAME.eq(EXECUTIONS_TOTAL), STATISTICS.S_COUNTER).otherwise(0)).as(
                TOTAL),
            sum(when(STATISTICS_FIELD.NAME.eq(EXECUTIONS_SKIPPED), STATISTICS.S_COUNTER).otherwise(
                0)).as(SKIPPED)
        )
        .from(LAUNCH)
        .leftJoin(STATISTICS)
        .on(LAUNCH.ID.eq(STATISTICS.LAUNCH_ID))
        .leftJoin(STATISTICS_FIELD)
        .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
        .and(STATISTICS_FIELD.NAME.in(EXECUTIONS_PASSED, EXECUTIONS_TOTAL, EXECUTIONS_SKIPPED))
        .where(LAUNCH.ID.eq(launchId))
        .groupBy(LAUNCH.ID)
        .fetchOneInto(PassingRateStatisticsResult.class);
  }

  @Override
  public PassingRateStatisticsResult summaryPassingRateStatistics(Filter filter, Sort sort,
      int limit) {
    return buildPassingRateSelect(filter, sort, limit).fetchInto(PassingRateStatisticsResult.class)
        .stream()
        .findFirst()
        .orElseThrow(() -> new ReportPortalException("No results for filter were found"));
  }

  @Override
  public List<ChartStatisticsContent> casesTrendStatistics(Filter filter, String contentField,
      Sort sort, int limit) {

    List<Field<?>> groupingFields = Lists.newArrayList(field(LAUNCH.ID),
        field(LAUNCH.NUMBER),
        field(LAUNCH.START_TIME),
        field(LAUNCH.NAME),
        fieldName(STATISTICS_TABLE, STATISTICS_COUNTER)
    );

    groupingFields.addAll(
        WidgetSortUtils.fieldTransformer(filter.getTarget()).apply(sort, LAUNCHES));

    return CASES_GROWTH_TREND_FETCHER.apply(dsl.with(LAUNCHES)
        .as(QueryBuilder.newBuilder(filter, collectJoinFields(filter, sort)).with(sort).with(limit)
            .build())
        .select(LAUNCH.ID,
            LAUNCH.NUMBER,
            LAUNCH.START_TIME,
            LAUNCH.NAME,
            fieldName(STATISTICS_TABLE, STATISTICS_COUNTER),
            coalesce(
                fieldName(STATISTICS_TABLE, STATISTICS_COUNTER).sub(lag(fieldName(STATISTICS_TABLE,
                    STATISTICS_COUNTER
                )).over().orderBy(LAUNCH.START_TIME.asc())), 0).as(DELTA)
        )
        .from(LAUNCH)
        .join(LAUNCHES)
        .on(LAUNCH.ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
        .leftJoin(dsl.select(STATISTICS.LAUNCH_ID, STATISTICS.S_COUNTER.as(STATISTICS_COUNTER),
                STATISTICS_FIELD.NAME.as(SF_NAME))
            .from(STATISTICS)
            .join(STATISTICS_FIELD)
            .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
            .where(STATISTICS_FIELD.NAME.eq(contentField))
            .asTable(STATISTICS_TABLE))
        .on(LAUNCH.ID.eq(fieldName(STATISTICS_TABLE, LAUNCH_ID).cast(Long.class)))
        .groupBy(groupingFields)
        .orderBy(LAUNCH.START_TIME.asc())
        .fetch(), contentField);
  }

  @Override
  public List<ChartStatisticsContent> bugTrendStatistics(Filter filter, List<String> contentFields,
      Sort sort, int limit) {

    return BUG_TREND_STATISTICS_FETCHER.apply(dsl.with(LAUNCHES)
        .as(QueryBuilder.newBuilder(filter, collectJoinFields(filter, sort)).with(sort).with(limit)
            .build())
        .select(LAUNCH.ID,
            LAUNCH.NAME,
            LAUNCH.NUMBER,
            LAUNCH.START_TIME,
            fieldName(STATISTICS_TABLE, SF_NAME),
            fieldName(STATISTICS_TABLE, STATISTICS_COUNTER)
        )
        .from(LAUNCH)
        .join(LAUNCHES)
        .on(LAUNCH.ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
        .leftJoin(dsl.select(STATISTICS.LAUNCH_ID, STATISTICS.S_COUNTER.as(STATISTICS_COUNTER),
                STATISTICS_FIELD.NAME.as(SF_NAME))
            .from(STATISTICS)
            .join(STATISTICS_FIELD)
            .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
            .where(STATISTICS_FIELD.NAME.in(contentFields))
            .asTable(STATISTICS_TABLE))
        .on(LAUNCH.ID.eq(fieldName(STATISTICS_TABLE, LAUNCH_ID).cast(Long.class)))
        .orderBy(LAUNCH.START_TIME.asc())
        .fetch());

  }

  @Override
  public List<ChartStatisticsContent> launchesComparisonStatistics(Filter filter,
      List<String> contentFields, Sort sort, int limit) {

    List<String> executionStatisticsFields = contentFields.stream()
        .filter(cf -> cf.contains(EXECUTIONS_KEY)).collect(toList());
    List<String> defectStatisticsFields = contentFields.stream()
        .filter(cf -> cf.contains(DEFECTS_KEY)).collect(toList());

    return LAUNCHES_STATISTICS_FETCHER.apply(dsl.with(LAUNCHES)
        .as(QueryBuilder.newBuilder(filter, collectJoinFields(filter, sort)).with(sort).with(limit)
            .build())
        .select(LAUNCH.ID,
            LAUNCH.NAME,
            LAUNCH.NUMBER,
            LAUNCH.START_TIME,
            field(name(STATISTICS_TABLE, SF_NAME), String.class),
            when(field(name(STATISTICS_TABLE, SF_NAME)).equalIgnoreCase(EXECUTIONS_TOTAL),
                field(name(STATISTICS_TABLE, STATISTICS_COUNTER)).cast(Double.class)
            ).otherwise(round(val(PERCENTAGE_MULTIPLIER).mul(
                        field(name(STATISTICS_TABLE, STATISTICS_COUNTER), Integer.class))
                    .div(nullif(dsl.select(DSL.sum(STATISTICS.S_COUNTER))
                        .from(STATISTICS)
                        .join(STATISTICS_FIELD)
                        .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
                        .where(STATISTICS.LAUNCH_ID.eq(LAUNCH.ID))
                        .and(STATISTICS_FIELD.NAME.in(executionStatisticsFields)
                            .and(STATISTICS_FIELD.NAME.notEqual(EXECUTIONS_TOTAL))), 0).cast(
                        Double.class)), 2))
                .as(fieldName(STATISTICS_TABLE, STATISTICS_COUNTER))
        )
        .from(LAUNCH)
        .join(LAUNCHES)
        .on(LAUNCH.ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
        .leftJoin(dsl.select(STATISTICS.LAUNCH_ID, STATISTICS.S_COUNTER.as(STATISTICS_COUNTER),
                STATISTICS_FIELD.NAME.as(SF_NAME))
            .from(STATISTICS)
            .join(STATISTICS_FIELD)
            .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
            .where(STATISTICS_FIELD.NAME.in(executionStatisticsFields))
            .asTable(STATISTICS_TABLE))
        .on(LAUNCH.ID.eq(fieldName(STATISTICS_TABLE, LAUNCH_ID).cast(Long.class)))
        .orderBy(WidgetSortUtils.sortingTransformer(filter.getTarget()).apply(sort, LAUNCHES))
        .unionAll(dsl.select(LAUNCH.ID,
                LAUNCH.NAME,
                LAUNCH.NUMBER,
                LAUNCH.START_TIME,
                field(name(STATISTICS_TABLE, SF_NAME), String.class),
                round(val(PERCENTAGE_MULTIPLIER).mul(
                            field(name(STATISTICS_TABLE, STATISTICS_COUNTER), Integer.class))
                        .div(nullif(dsl.select(DSL.sum(STATISTICS.S_COUNTER))
                            .from(STATISTICS)
                            .join(STATISTICS_FIELD)
                            .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
                            .where(STATISTICS.LAUNCH_ID.eq(LAUNCH.ID))
                            .and(STATISTICS_FIELD.NAME.in(defectStatisticsFields)), 0).cast(Double.class)),
                    2)
            )
            .from(LAUNCH)
            .join(LAUNCHES)
            .on(LAUNCH.ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
            .leftJoin(dsl.select(STATISTICS.LAUNCH_ID,
                    STATISTICS.S_COUNTER.as(STATISTICS_COUNTER),
                    STATISTICS_FIELD.NAME.as(SF_NAME)
                )
                .from(STATISTICS)
                .join(STATISTICS_FIELD)
                .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
                .where(STATISTICS_FIELD.NAME.in(defectStatisticsFields))
                .asTable(STATISTICS_TABLE))
            .on(LAUNCH.ID.eq(fieldName(STATISTICS_TABLE, LAUNCH_ID).cast(Long.class)))
            .orderBy(WidgetSortUtils.sortingTransformer(filter.getTarget()).apply(sort, LAUNCHES)))
        .fetch());

  }

  @Override
  public List<LaunchesDurationContent> launchesDurationStatistics(Filter filter, Sort sort,
      boolean isLatest, int limit) {

    return dsl.with(LAUNCHES)
        .as(QueryUtils.createQueryBuilderWithLatestLaunchesOption(filter, sort, isLatest).with(sort)
            .with(limit).build())
        .select(LAUNCH.ID,
            LAUNCH.NAME,
            LAUNCH.NUMBER,
            LAUNCH.STATUS,
            LAUNCH.START_TIME,
            LAUNCH.END_TIME,
            timestampDiff(LAUNCH.END_TIME.cast(Timestamp.class),
                LAUNCH.START_TIME.cast(Timestamp.class)).as(DURATION)
        )
        .from(LAUNCH)
        .join(LAUNCHES)
        .on(LAUNCH.ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
        .orderBy(WidgetSortUtils.sortingTransformer(filter.getTarget()).apply(sort, LAUNCHES))
        .fetchInto(LaunchesDurationContent.class);
  }

  @Override
  public List<NotPassedCasesContent> notPassedCasesStatistics(Filter filter, Sort sort, int limit) {

    return dsl.with(LAUNCHES)
        .as(QueryBuilder.newBuilder(filter, collectJoinFields(filter, sort)).with(sort).with(limit)
            .build())
        .select(LAUNCH.ID,
            LAUNCH.NAME,
            LAUNCH.NUMBER,
            LAUNCH.START_TIME,
            fieldName(STATISTICS_TABLE, STATISTICS_COUNTER),
            coalesce(round(val(PERCENTAGE_MULTIPLIER).mul(dsl.select(DSL.sum(STATISTICS.S_COUNTER))
                    .from(STATISTICS)
                    .join(STATISTICS_FIELD)
                    .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
                    .where(STATISTICS_FIELD.NAME.in(EXECUTIONS_SKIPPED, EXECUTIONS_FAILED))
                    .and(STATISTICS.LAUNCH_ID.eq(LAUNCH.ID))
                    .asField()
                    .cast(Double.class))
                .div(nullif(field(name(STATISTICS_TABLE, STATISTICS_COUNTER), Integer.class),
                    0).cast(Double.class)), 2), 0)
                .as(PERCENTAGE)
        )
        .from(LAUNCH)
        .join(LAUNCHES)
        .on(LAUNCH.ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
        .leftJoin(dsl.select(STATISTICS.LAUNCH_ID, STATISTICS.S_COUNTER.as(STATISTICS_COUNTER),
                STATISTICS_FIELD.NAME.as(SF_NAME))
            .from(STATISTICS)
            .join(STATISTICS_FIELD)
            .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
            .where(STATISTICS_FIELD.NAME.eq(EXECUTIONS_TOTAL))
            .asTable(STATISTICS_TABLE))
        .on(LAUNCH.ID.eq(fieldName(STATISTICS_TABLE, LAUNCH_ID).cast(Long.class)))
        .orderBy(LAUNCH.START_TIME.asc())
        .fetch(NOT_PASSED_CASES_CONTENT_RECORD_MAPPER);
  }

  @Override
  public List<LaunchesTableContent> launchesTableStatistics(Filter filter,
      List<String> contentFields, Sort sort, int limit) {

    Map<String, String> criteria = filter.getTarget()
        .getCriteriaHolders()
        .stream()
        .collect(
            Collectors.toMap(CriteriaHolder::getFilterCriteria, CriteriaHolder::getQueryCriteria));

    boolean isAttributePresent = contentFields.remove("attributes");

    List<Field<?>> selectFields = contentFields.stream()
        .filter(cf -> !cf.startsWith(STATISTICS_KEY))
        .map(cf -> field(ofNullable(criteria.get(cf)).orElseThrow(
            () -> new ReportPortalException(Suppliers.formattedSupplier(
                "Unknown table field - '{}'",
                cf
            ).get()))))
        .collect(Collectors.toList());

    Collections.addAll(selectFields, LAUNCH.ID, fieldName(STATISTICS_TABLE, STATISTICS_COUNTER),
        fieldName(STATISTICS_TABLE, SF_NAME));

    if (isAttributePresent) {
      Collections.addAll(selectFields, ITEM_ATTRIBUTE.ID.as(ATTR_ID), ITEM_ATTRIBUTE.KEY,
          ITEM_ATTRIBUTE.VALUE);
    }

    List<String> statisticsFields = contentFields.stream()
        .filter(cf -> cf.startsWith(STATISTICS_KEY)).collect(toList());

    return LAUNCHES_TABLE_FETCHER.apply(
        buildLaunchesTableQuery(selectFields, statisticsFields, filter, sort, limit,
            isAttributePresent)
            .fetch(), contentFields);

  }

  @Override
  public List<ActivityResource> activityStatistics(Filter filter, Sort sort, int limit) {
    return dsl.fetch(QueryBuilder.newBuilder(filter).with(sort).with(limit).wrap().build())
        .map(ACTIVITY_MAPPER);
  }

  @Override
  public Map<String, UniqueBugContent> uniqueBugStatistics(Filter filter, Sort sort,
      boolean isLatest, int limit) {

    Map<String, UniqueBugContent> content = UNIQUE_BUG_CONTENT_FETCHER.apply(dsl.with(LAUNCHES)
        .as(QueryUtils.createQueryBuilderWithLatestLaunchesOption(filter, sort, isLatest)
            .with(limit).with(sort).build())
        .select(TICKET.TICKET_ID,
            TICKET.SUBMIT_DATE,
            TICKET.URL,
            TICKET.SUBMITTER,
            TEST_ITEM.ITEM_ID,
            TEST_ITEM.NAME,
            TEST_ITEM.PATH,
            TEST_ITEM.LAUNCH_ID,
            fieldName(ITEM_ATTRIBUTES, KEY),
            fieldName(ITEM_ATTRIBUTES, VALUE)
        )
        .from(TEST_ITEM)
        .join(LAUNCHES)
        .on(fieldName(LAUNCHES, ID).cast(Long.class).eq(TEST_ITEM.LAUNCH_ID))
        .join(TEST_ITEM_RESULTS)
        .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .leftJoin(ISSUE)
        .on(TEST_ITEM.ITEM_ID.eq(ISSUE.ISSUE_ID))
        .leftJoin(ISSUE_TICKET)
        .on(ISSUE.ISSUE_ID.eq(ISSUE_TICKET.ISSUE_ID))
        .join(TICKET)
        .on(ISSUE_TICKET.TICKET_ID.eq(TICKET.ID))
        .leftJoin(
            lateral(dsl.select(ITEM_ATTRIBUTE.ITEM_ID, ITEM_ATTRIBUTE.KEY, ITEM_ATTRIBUTE.VALUE)
                .from(ITEM_ATTRIBUTE)
                .where(
                    ITEM_ATTRIBUTE.ITEM_ID.eq(TEST_ITEM.ITEM_ID).andNot(ITEM_ATTRIBUTE.SYSTEM))).as(
                ITEM_ATTRIBUTES))
        .on(TEST_ITEM.ITEM_ID.eq(fieldName(ITEM_ATTRIBUTES, ITEM_ID).cast(Long.class)))
        .orderBy(TICKET.SUBMIT_DATE.desc())
        .fetch());

    return content;
  }

  @Override
  public Map<String, List<ProductStatusStatisticsContent>> productStatusGroupedByFilterStatistics(
      Map<Filter, Sort> filterSortMapping,
      List<String> contentFields, Map<String, String> customColumns, boolean isLatest, int limit) {

    Select<? extends Record> select = filterSortMapping.entrySet()
        .stream()
        .map(f -> (Select<? extends Record>) buildFilterGroupedQuery(f.getKey(),
            isLatest,
            f.getValue(),
            limit,
            contentFields,
            customColumns
        ))
        .collect(Collectors.toList())
        .stream()
        .reduce(Select::unionAll)
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "Query building for Product Status Widget failed"
        ));

    Map<String, List<ProductStatusStatisticsContent>> productStatusContent = PRODUCT_STATUS_FILTER_GROUPED_FETCHER.apply(
        select.fetch(),
        customColumns
    );

    productStatusContent.put(TOTAL, countFilterTotalStatistics(productStatusContent));

    return productStatusContent;
  }

  @Override
  public List<ProductStatusStatisticsContent> productStatusGroupedByLaunchesStatistics(
      Filter filter, List<String> contentFields,
      Map<String, String> customColumns, Sort sort, boolean isLatest, int limit) {

    List<Field<?>> selectFields = getCommonProductStatusFields(filter, contentFields);

    List<ProductStatusStatisticsContent> productStatusStatisticsResult = PRODUCT_STATUS_LAUNCH_GROUPED_FETCHER.apply(
        buildProductStatusQuery(filter,
            isLatest,
            sort,
            limit,
            selectFields,
            contentFields,
            customColumns
        ).orderBy(WidgetSortUtils.sortingTransformer(filter.getTarget()).apply(sort, LAUNCHES))
            .fetch(),
        customColumns
    );

    if (!productStatusStatisticsResult.isEmpty()) {
      productStatusStatisticsResult.add(countLaunchTotalStatistics(productStatusStatisticsResult));
    }
    return productStatusStatisticsResult;
  }

  @Override
  public List<MostTimeConsumingTestCasesContent> mostTimeConsumingTestCasesStatistics(Filter filter,
      int limit) {
    final Set<String> fields = collectJoinFields(filter);
    fields.add(CRITERIA_DURATION);
    final SelectQuery<? extends Record> filteringQuery = QueryBuilder.newBuilder(filter, fields)
        .with(limit)
        .build();
    filteringQuery.addOrderBy(max(TEST_ITEM_RESULTS.DURATION).desc());
    return dsl.with(ITEMS)
        .as(filteringQuery)
        .select(TEST_ITEM.ITEM_ID.as(ID),
            TEST_ITEM.UNIQUE_ID,
            TEST_ITEM.NAME,
            TEST_ITEM.TYPE,
            TEST_ITEM.PATH,
            TEST_ITEM.START_TIME,
            TEST_ITEM_RESULTS.END_TIME,
            TEST_ITEM_RESULTS.DURATION,
            TEST_ITEM_RESULTS.STATUS
        )
        .from(TEST_ITEM)
        .join(ITEMS)
        .on(fieldName(ITEMS, ID).cast(Long.class).eq(TEST_ITEM.ITEM_ID))
        .join(TEST_ITEM_RESULTS)
        .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .orderBy(fieldName(TEST_ITEM_RESULTS.DURATION).desc())
        .fetchInto(MostTimeConsumingTestCasesContent.class);
  }

  @Override
  public List<TopPatternTemplatesContent> patternTemplate(Filter filter, Sort sort,
      @Nullable String attributeKey,
      @Nullable String patternName, boolean isLatest, int launchesLimit, int attributesLimit) {

    Condition attributeKeyCondition = ofNullable(attributeKey).map(ITEM_ATTRIBUTE.KEY::eq)
        .orElseGet(DSL::noCondition);
    Field<?> launchIdsField = isLatest ? DSL.max(LAUNCH.ID).as(ID) : DSL.arrayAgg(LAUNCH.ID).as(ID);
    List<Field<?>> groupingFields = isLatest ?
        Lists.newArrayList(LAUNCH.NAME, ITEM_ATTRIBUTE.VALUE) :
        Lists.newArrayList(ITEM_ATTRIBUTE.VALUE);

    Map<String, List<Long>> attributeIdsMapping = PATTERN_TEMPLATES_AGGREGATION_FETCHER.apply(
        dsl.with(LAUNCHES)
            .as(QueryBuilder.newBuilder(filter, collectJoinFields(filter, sort)).with(sort)
                .with(launchesLimit).build())
            .select(launchIdsField, ITEM_ATTRIBUTE.VALUE)
            .from(LAUNCH)
            .join(LAUNCHES)
            .on(fieldName(LAUNCHES, ID).cast(Long.class).eq(LAUNCH.ID))
            .join(ITEM_ATTRIBUTE)
            .on(LAUNCH.ID.eq(ITEM_ATTRIBUTE.LAUNCH_ID))
            .where(attributeKeyCondition)
            .and(ITEM_ATTRIBUTE.VALUE.in(dsl.select(ITEM_ATTRIBUTE.VALUE)
                .from(ITEM_ATTRIBUTE)
                .join(LAUNCHES)
                .on(fieldName(LAUNCHES, ID).cast(Long.class).eq(ITEM_ATTRIBUTE.LAUNCH_ID))
                .where(attributeKeyCondition)
                .groupBy(ITEM_ATTRIBUTE.VALUE)
                .orderBy(when(ITEM_ATTRIBUTE.VALUE.likeRegex(VERSION_PATTERN),
                    PostgresDSL.stringToArray(ITEM_ATTRIBUTE.VALUE, VERSION_DELIMITER)
                        .cast(Integer[].class)
                ).desc(), ITEM_ATTRIBUTE.VALUE.sort(SortOrder.DESC))
                .limit(attributesLimit)))
            .groupBy(groupingFields)
            .orderBy(when(ITEM_ATTRIBUTE.VALUE.likeRegex(VERSION_PATTERN),
                PostgresDSL.stringToArray(ITEM_ATTRIBUTE.VALUE, VERSION_DELIMITER)
                    .cast(Integer[].class)
            ).desc(), ITEM_ATTRIBUTE.VALUE.sort(SortOrder.DESC))
            .fetch(), isLatest);

    return StringUtils.isBlank(patternName) ?
        buildPatternTemplatesQuery(attributeIdsMapping) :
        buildPatternTemplatesQueryGroupedByPattern(attributeIdsMapping, patternName);

  }

  @Override
  public List<ComponentHealthCheckContent> componentHealthCheck(Filter launchFilter,
      Sort launchSort, boolean isLatest, int launchesLimit,
      Filter testItemFilter, String currentLevelKey, boolean excludeSkipped) {

    Table<? extends Record> launchesTable = QueryUtils.createQueryBuilderWithLatestLaunchesOption(
            launchFilter, launchSort, isLatest)
        .with(launchesLimit)
        .with(launchSort)
        .build()
        .asTable(LAUNCHES);

    return COMPONENT_HEALTH_CHECK_FETCHER.apply(dsl.select(fieldName(ITEMS, VALUE),
            DSL.count(fieldName(ITEMS, ITEM_ID)).as(TOTAL),
            round(DSL.val(PERCENTAGE_MULTIPLIER)
                .mul(DSL.count(fieldName(ITEMS, ITEM_ID))
                    .filterWhere(
                        fieldName(ITEMS, STATUS).cast(JStatusEnum.class).eq(JStatusEnum.PASSED)))
                .div(nullif(DSL.count(fieldName(ITEMS, ITEM_ID)), 0)), 2).as(PASSING_RATE)
        )
        .from(dsl.with(ITEMS)
            .as(QueryBuilder.newBuilder(testItemFilter, collectJoinFields(testItemFilter))
                .addJointToStart(launchesTable,
                    JoinType.JOIN,
                    TEST_ITEM.LAUNCH_ID.eq(fieldName(launchesTable.getName(), ID).cast(Long.class))
                )
                .build())
            .select(TEST_ITEM.ITEM_ID, TEST_ITEM_RESULTS.STATUS, ITEM_ATTRIBUTE.KEY,
                ITEM_ATTRIBUTE.VALUE)
            .from(TEST_ITEM)
            .join(ITEMS)
            .on(TEST_ITEM.ITEM_ID.eq(fieldName(ITEMS, ID).cast(Long.class)))
            .join(TEST_ITEM_RESULTS)
            .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
            .join(ITEM_ATTRIBUTE)
            .on((TEST_ITEM.ITEM_ID.eq(ITEM_ATTRIBUTE.ITEM_ID)
                .or(TEST_ITEM.LAUNCH_ID.eq(ITEM_ATTRIBUTE.LAUNCH_ID))).and(
                ITEM_ATTRIBUTE.KEY.eq(currentLevelKey).and(ITEM_ATTRIBUTE.SYSTEM.isFalse())))
            .groupBy(TEST_ITEM.ITEM_ID, TEST_ITEM_RESULTS.STATUS, ITEM_ATTRIBUTE.KEY,
                ITEM_ATTRIBUTE.VALUE)
            .having(filterSkippedTests(excludeSkipped))
            .asTable(ITEMS))
        .groupBy(fieldName(ITEMS, VALUE))
        .orderBy(round(DSL.val(PERCENTAGE_MULTIPLIER)
            .mul(DSL.count(fieldName(ITEMS, ITEM_ID))
                .filterWhere(
                    fieldName(ITEMS, STATUS).cast(JStatusEnum.class).eq(JStatusEnum.PASSED)))
            .div(nullif(DSL.count(fieldName(ITEMS, ITEM_ID)), 0)), 2))
        .fetch());
  }

  private Condition filterSkippedTests(boolean excludeSkipped) {
    Condition condition = DSL.noCondition();
    if (excludeSkipped) {
      return DSL.notExists(
          dsl.selectOne().from(STATISTICS).join(STATISTICS_FIELD)
              .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
              .where(TEST_ITEM.ITEM_ID.eq(STATISTICS.ITEM_ID)
                  .and(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
                  .and(STATISTICS_FIELD.NAME.eq(EXECUTIONS_SKIPPED))));
    }
    return condition;
  }

  @Override
  public void generateCumulativeTrendChartView(boolean refresh, String viewName,
      Filter launchFilter, Sort launchesSort,
      List<String> attributes, int launchesLimit) {

    if (refresh) {
      removeWidgetView(viewName);
    }

    final String FIRST_LEVEL = "first_level";

    final SelectJoinStep<Record5<Long, String, Long, String, String>> FIRST_LEVEL_TABLE = dsl.with(
            FIRST_LEVEL)
        .as(dsl.with(LAUNCHES)
            .as(QueryBuilder.newBuilder(launchFilter, collectJoinFields(launchFilter))
                .with(launchesSort)
                .with(launchesLimit)
                .build())
            .select(max(LAUNCH.ID).as(ID),
                LAUNCH.NAME,
                arrayAggDistinct(LAUNCH.ID).as(AGGREGATED_LAUNCHES_IDS),
                ITEM_ATTRIBUTE.KEY.as(ATTRIBUTE_KEY),
                ITEM_ATTRIBUTE.VALUE.as(ATTRIBUTE_VALUE)
            )
            .from(LAUNCH)
            .join(LAUNCHES)
            .on(LAUNCH.ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
            .join(ITEM_ATTRIBUTE)
            .on(LAUNCH.ID.eq(ITEM_ATTRIBUTE.LAUNCH_ID))
            .and(ITEM_ATTRIBUTE.KEY.eq(attributes.get(0)).and(ITEM_ATTRIBUTE.SYSTEM.isFalse()))
            .groupBy(LAUNCH.NAME, ITEM_ATTRIBUTE.KEY, ITEM_ATTRIBUTE.VALUE))
        .select(fieldName(FIRST_LEVEL, ID).cast(Long.class).as(ID),
            fieldName(FIRST_LEVEL, NAME).cast(String.class).as(NAME),
            val(null, fieldName(FIRST_LEVEL, ID).cast(Long.class)).as(FIRST_LEVEL_ID),
            fieldName(FIRST_LEVEL, ATTRIBUTE_KEY).cast(String.class).as(ATTRIBUTE_KEY),
            fieldName(FIRST_LEVEL, ATTRIBUTE_VALUE).cast(String.class).as(ATTRIBUTE_VALUE)
        )
        .from(FIRST_LEVEL);

    SelectQuery<Record5<Long, String, Long, String, String>> query;

    if (attributes.size() == 2 && attributes.get(1) != null) {
      final SelectHavingStep<Record5<Long, String, Long, String, String>> SECOND_LEVEL_TABLE = dsl.select(
              max(LAUNCH.ID).as(ID),
              LAUNCH.NAME,
              max(fieldName(FIRST_LEVEL, ID)).cast(Long.class).as(FIRST_LEVEL_ID),
              ITEM_ATTRIBUTE.KEY.as(ATTRIBUTE_KEY),
              ITEM_ATTRIBUTE.VALUE.as(ATTRIBUTE_VALUE)
          )
          .from(FIRST_LEVEL)
          .join(LAUNCH)
          .on(Suppliers.formattedSupplier("{} = any({})", LAUNCH.ID, AGGREGATED_LAUNCHES_IDS).get())
          .join(ITEM_ATTRIBUTE)
          .on(LAUNCH.ID.eq(ITEM_ATTRIBUTE.LAUNCH_ID))
          .and(ITEM_ATTRIBUTE.KEY.eq(attributes.get(1)).and(ITEM_ATTRIBUTE.SYSTEM.isFalse()))
          .groupBy(LAUNCH.NAME,
              fieldName(FIRST_LEVEL, ATTRIBUTE_KEY),
              fieldName(FIRST_LEVEL, ATTRIBUTE_VALUE),
              ITEM_ATTRIBUTE.KEY,
              ITEM_ATTRIBUTE.VALUE
          );
      query = FIRST_LEVEL_TABLE.union(SECOND_LEVEL_TABLE).getQuery();
    } else {
      query = FIRST_LEVEL_TABLE.getQuery();
    }
    dsl.execute(DSL.sql(String.format("CREATE MATERIALIZED VIEW %s AS (%s)", name(viewName),
        query.toString())));
  }

  @Override
  public List<CumulativeTrendChartEntry> cumulativeTrendChart(String viewName,
      String levelAttributeKey, @Nullable String subAttributeKey,
      @Nullable String parentAttribute) {
    final SelectOnConditionStep<? extends Record4> baseQuery = dsl.select(
            DSL.arrayAgg(fieldName(viewName, ID)).as(LAUNCHES),
            fieldName(viewName, ATTRIBUTE_VALUE),
            STATISTICS_FIELD.NAME,
            sum(STATISTICS.S_COUNTER).as(STATISTICS_COUNTER)
        )
        .from(viewName)
        .join(STATISTICS)
        .on(fieldName(viewName, ID).cast(Long.class).eq(STATISTICS.LAUNCH_ID))
        .join(STATISTICS_FIELD)
        .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID));

    if (parentAttribute != null) {
      String[] split = parentAttribute.split(KEY_VALUE_SEPARATOR);

      final SelectConditionStep<Record1<Long>> subLevelLaunches = selectDistinct(
          fieldName(viewName, ID).cast(Long.class)).from(
              viewName)
          .where(fieldName(viewName, ATTRIBUTE_KEY).cast(String.class).eq(split[0]))
          .and(fieldName(viewName, ATTRIBUTE_VALUE).cast(String.class).eq(split[1]));
      baseQuery.where(fieldName(viewName, FIRST_LEVEL_ID).cast(Long.class).in(subLevelLaunches));
    }

    List<CumulativeTrendChartEntry> accumulatedLaunches = CUMULATIVE_TREND_CHART_FETCHER.apply(
        baseQuery.where(fieldName(ATTRIBUTE_KEY).cast(
                String.class).eq(levelAttributeKey))
            .groupBy(fieldName(viewName, ATTRIBUTE_VALUE), STATISTICS_FIELD.NAME)
            .orderBy(when(fieldName(viewName, ATTRIBUTE_VALUE).likeRegex(VERSION_PATTERN),
                PostgresDSL.stringToArray(field(name(viewName, ATTRIBUTE_VALUE), String.class),
                        VERSION_DELIMITER)
                    .cast(Integer[].class)
            ), fieldName(viewName, ATTRIBUTE_VALUE).sort(SortOrder.ASC))
            .fetch());

    if (!StringUtils.isEmpty(subAttributeKey)) {
      accumulatedLaunches.forEach(
          attributeLaunches -> CUMULATIVE_TOOLTIP_FETCHER.accept(attributeLaunches,
              dsl.selectDistinct(fieldName(viewName, ATTRIBUTE_KEY),
                      fieldName(viewName, ATTRIBUTE_VALUE))
                  .from(viewName)
                  .where(fieldName(viewName, ATTRIBUTE_KEY).cast(String.class)
                      .eq(subAttributeKey)
                      .and(fieldName(viewName, ID).in(
                          attributeLaunches.getContent().getLaunchIds())))
                  .fetch()
          ));
    }
    return accumulatedLaunches;
  }

  @Override
  public List<Long> getCumulativeLevelRedirectLaunchIds(String viewName, String attributes) {
    String[] attributeLevels = attributes.split(VALUES_SEPARATOR);

    SelectConditionStep<Record1<Long>> firstLevelCondition = dsl.select(
            fieldName(viewName, ID).cast(Long.class))
        .from(viewName)
        .where(concat(coalesce(fieldName(viewName, ATTRIBUTE_KEY), ""),
            val(KEY_VALUE_SEPARATOR),
            fieldName(viewName, ATTRIBUTE_VALUE)
        ).eq(attributeLevels[0]));

    if (attributeLevels.length == 2) {
      return dsl.select(fieldName(viewName, ID))
          .from(viewName)
          .where(concat(coalesce(fieldName(viewName, ATTRIBUTE_KEY), ""),
              val(KEY_VALUE_SEPARATOR),
              fieldName(viewName, ATTRIBUTE_VALUE)
          ).eq(attributeLevels[1]))
          .and(fieldName(viewName, FIRST_LEVEL_ID).cast(Long.class).in(firstLevelCondition))
          .fetchInto(Long.class);
    }

    return firstLevelCondition.fetchInto(Long.class);
  }

  @Override
  public void generateComponentHealthCheckTable(boolean refresh, HealthCheckTableInitParams params,
      Filter launchFilter, Sort launchSort,
      int launchesLimit, boolean isLatest) {

    if (refresh) {
      removeWidgetView(params.getViewName());
    }

    Table<? extends Record> launchesTable = QueryUtils.createQueryBuilderWithLatestLaunchesOption(
            launchFilter, launchSort, isLatest)
        .with(launchesLimit)
        .with(launchSort)
        .build()
        .asTable(LAUNCHES);

    List<Field<?>> selectFields = Lists.newArrayList(TEST_ITEM.ITEM_ID, ITEM_ATTRIBUTE.KEY,
        ITEM_ATTRIBUTE.VALUE);

    ofNullable(params.getCustomKey()).ifPresent(
        key -> selectFields.add(DSL.arrayAggDistinct(fieldName(CUSTOM_ATTRIBUTE, VALUE))
            .filterWhere(fieldName(CUSTOM_ATTRIBUTE, VALUE).isNotNull())
            .as(CUSTOM_COLUMN)));

    SelectOnConditionStep<Record> baseQuery = dsl.select(selectFields).from(TEST_ITEM)
        .join(launchesTable)
        .on(TEST_ITEM.LAUNCH_ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
        .join(TEST_ITEM_RESULTS)
        .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .join(ITEM_ATTRIBUTE)
        .on(and(TEST_ITEM.ITEM_ID.eq(ITEM_ATTRIBUTE.ITEM_ID)
            .or(TEST_ITEM.LAUNCH_ID.eq(ITEM_ATTRIBUTE.LAUNCH_ID)))
            .and(ITEM_ATTRIBUTE.KEY.in(params.getAttributeKeys())).and(ITEM_ATTRIBUTE.SYSTEM.isFalse()));

    dsl.execute(dsl.renderInlined(dsl.createMaterializedView(params.getViewName())
        .as(ofNullable(params.getCustomKey()).map(key -> {
              JItemAttribute customAttribute = ITEM_ATTRIBUTE.as(CUSTOM_ATTRIBUTE);
              return baseQuery.leftJoin(customAttribute)
                  .on(DSL.condition(Operator.OR,
                      TEST_ITEM.ITEM_ID.eq(customAttribute.ITEM_ID),
                      TEST_ITEM.LAUNCH_ID.eq(customAttribute.LAUNCH_ID)
                  ).and(customAttribute.KEY.eq(key)));
            })
            .orElse(baseQuery)
            .where(TEST_ITEM.HAS_STATS.isTrue()
                .and(TEST_ITEM.HAS_CHILDREN.isFalse())
                .and(TEST_ITEM.TYPE.eq(JTestItemTypeEnum.STEP))
                .and(TEST_ITEM.RETRY_OF.isNull())
                .and(TEST_ITEM_RESULTS.STATUS.notEqual(JStatusEnum.IN_PROGRESS)))
            .groupBy(TEST_ITEM.ITEM_ID, ITEM_ATTRIBUTE.KEY, ITEM_ATTRIBUTE.VALUE)
            .getQuery())));
  }

  @Override
  public void removeWidgetView(String viewName) {
    dsl.execute(DSL.sql(
        Suppliers.formattedSupplier("DROP MATERIALIZED VIEW IF EXISTS {}", name(viewName))
            .get()));
  }

  @Override
  public List<HealthCheckTableContent> componentHealthCheckTable(HealthCheckTableGetParams params) {
    return healthCheckTableChain.apply(params);
  }

  private SelectSeekStepN<? extends Record> buildLaunchesTableQuery(
      Collection<Field<?>> selectFields,
      Collection<String> statisticsFields, Filter filter, Sort sort, int limit,
      boolean isAttributePresent) {

    SelectOnConditionStep<? extends Record> select = dsl.with(LAUNCHES)
        .as(QueryBuilder.newBuilder(filter, collectJoinFields(filter, sort)).with(sort).with(limit)
            .build())
        .select(selectFields)
        .from(LAUNCH)
        .join(LAUNCHES)
        .on(LAUNCH.ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
        .leftJoin(dsl.select(STATISTICS.LAUNCH_ID, STATISTICS.S_COUNTER.as(STATISTICS_COUNTER),
                STATISTICS_FIELD.NAME.as(SF_NAME))
            .from(STATISTICS)
            .join(STATISTICS_FIELD)
            .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
            .where(STATISTICS_FIELD.NAME.in(statisticsFields))
            .asTable(STATISTICS_TABLE))
        .on(LAUNCH.ID.eq(fieldName(STATISTICS_TABLE, LAUNCH_ID).cast(Long.class)))
        .join(USERS)
        .on(LAUNCH.USER_ID.eq(USERS.ID));
    if (isAttributePresent) {
      select = select.leftJoin(ITEM_ATTRIBUTE).on(LAUNCH.ID.eq(ITEM_ATTRIBUTE.LAUNCH_ID))
          .and(ITEM_ATTRIBUTE.SYSTEM.isFalse());
    }

    return select.orderBy(
        WidgetSortUtils.sortingTransformer(filter.getTarget()).apply(sort, LAUNCHES));

  }

  private SelectOnConditionStep<? extends Record> buildPassingRateSelect(Filter filter, Sort sort,
      int limit) {
    return dsl.with(LAUNCHES)
        .as(QueryBuilder.newBuilder(filter, collectJoinFields(filter, sort)).with(sort).with(limit)
            .build())
        .select(
            sum(when(fieldName(STATISTICS_TABLE, SF_NAME).cast(String.class).eq(EXECUTIONS_PASSED),
                fieldName(STATISTICS_TABLE, STATISTICS_COUNTER).cast(Integer.class)
            ).otherwise(0)).as(PASSED),
            sum(when(fieldName(STATISTICS_TABLE, SF_NAME).cast(String.class).eq(EXECUTIONS_TOTAL),
                fieldName(STATISTICS_TABLE, STATISTICS_COUNTER).cast(Integer.class)
            ).otherwise(0)).as(TOTAL),
            sum(when(fieldName(STATISTICS_TABLE, SF_NAME).cast(String.class).eq(EXECUTIONS_SKIPPED),
                fieldName(STATISTICS_TABLE, STATISTICS_COUNTER).cast(Integer.class)
            ).otherwise(0)).as(SKIPPED),
            max(LAUNCH.NUMBER).as(NUMBER)
        )
        .from(LAUNCH)
        .join(LAUNCHES)
        .on(LAUNCH.ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
        .leftJoin(dsl.select(STATISTICS.LAUNCH_ID, STATISTICS.S_COUNTER.as(STATISTICS_COUNTER),
                STATISTICS_FIELD.NAME.as(SF_NAME))
            .from(STATISTICS)
            .join(STATISTICS_FIELD)
            .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
            .where(
                STATISTICS_FIELD.NAME.in(EXECUTIONS_PASSED, EXECUTIONS_TOTAL, EXECUTIONS_SKIPPED))
            .asTable(STATISTICS_TABLE))
        .on(LAUNCH.ID.eq(fieldName(STATISTICS_TABLE, LAUNCH_ID).cast(Long.class)));

  }

  private SelectSeekStepN<? extends Record> buildFilterGroupedQuery(Filter filter, boolean isLatest,
      Sort sort, int limit,
      Collection<String> contentFields, Map<String, String> customColumns) {

    List<Field<?>> fields = getCommonProductStatusFields(filter, contentFields);
    fields.add(dsl.selectDistinct(FILTER.NAME).from(FILTER).where(FILTER.ID.eq(filter.getId()))
        .asField(FILTER_NAME));

    return buildProductStatusQuery(filter,
        isLatest,
        sort,
        limit,
        fields,
        contentFields,
        customColumns
    ).orderBy(WidgetSortUtils.sortingTransformer(filter.getTarget()).apply(sort, LAUNCHES));

  }

  private List<Field<?>> getCommonProductStatusFields(Filter filter,
      Collection<String> contentFields) {

    Map<String, String> criteria = filter.getTarget()
        .getCriteriaHolders()
        .stream()
        .collect(
            Collectors.toMap(CriteriaHolder::getFilterCriteria, CriteriaHolder::getQueryCriteria));

    List<Field<?>> selectFields = contentFields.stream()
        .filter(cf -> !cf.startsWith(STATISTICS_KEY))
        .map(criteria::get)
        .filter(Objects::nonNull)
        .map(DSL::field)
        .collect(Collectors.toList());

    Collections.addAll(selectFields,
        LAUNCH.ID,
        LAUNCH.NAME,
        LAUNCH.NUMBER,
        fieldName(STATISTICS_TABLE, SF_NAME),
        fieldName(STATISTICS_TABLE, STATISTICS_COUNTER),
        round(val(PERCENTAGE_MULTIPLIER).mul(dsl.select(sum(STATISTICS.S_COUNTER))
                .from(STATISTICS)
                .join(STATISTICS_FIELD)
                .onKey()
                .where(
                    STATISTICS_FIELD.NAME.eq(EXECUTIONS_PASSED).and(STATISTICS.LAUNCH_ID.eq(LAUNCH.ID)))
                .asField()
                .cast(Double.class))
            .div(nullif(dsl.select(sum(STATISTICS.S_COUNTER))
                .from(STATISTICS)
                .join(STATISTICS_FIELD)
                .onKey()
                .where(STATISTICS_FIELD.NAME.eq(EXECUTIONS_TOTAL)
                    .and(STATISTICS.LAUNCH_ID.eq(LAUNCH.ID)))
                .asField(), 0)), 2).as(PASSING_RATE),
        timestampDiff(LAUNCH.END_TIME.cast(Timestamp.class),
            LAUNCH.START_TIME.cast(Timestamp.class))
            .as(DURATION)
    );

    return selectFields;
  }

  private SelectOnConditionStep<? extends Record> buildProductStatusQuery(Filter filter,
      boolean isLatest, Sort sort, int limit,
      Collection<Field<?>> fields, Collection<String> contentFields,
      Map<String, String> customColumns) {

    List<Condition> attributesKeyConditions = customColumns.values()
        .stream()
        .map(customColumn -> ofNullable(customColumn).map(ITEM_ATTRIBUTE.KEY::eq)
            .orElseGet(ITEM_ATTRIBUTE.KEY::isNull))
        .collect(Collectors.toList());

    Optional<Condition> combinedAttributeKeyCondition = attributesKeyConditions.stream()
        .reduce(Condition::or);

    List<String> statisticsFields = contentFields.stream()
        .filter(cf -> cf.startsWith(STATISTICS_KEY)).collect(toList());

    return combinedAttributeKeyCondition.map(c -> {
      Collections.addAll(fields,
          fieldName(ATTR_TABLE, ATTR_ID),
          fieldName(ATTR_TABLE, ATTRIBUTE_VALUE),
          fieldName(ATTR_TABLE, ATTRIBUTE_KEY)
      );
      return getProductStatusSelect(filter, isLatest, sort, limit, fields,
          statisticsFields).leftJoin(dsl.select(ITEM_ATTRIBUTE.ID.as(
                  ATTR_ID),
              ITEM_ATTRIBUTE.VALUE.as(ATTRIBUTE_VALUE),
              ITEM_ATTRIBUTE.KEY.as(ATTRIBUTE_KEY),
              ITEM_ATTRIBUTE.LAUNCH_ID.as(LAUNCH_ID)
          ).from(ITEM_ATTRIBUTE).where(c).asTable(ATTR_TABLE))
          .on(LAUNCH.ID.eq(fieldName(ATTR_TABLE, LAUNCH_ID).cast(Long.class)));
    }).orElseGet(
        () -> getProductStatusSelect(filter, isLatest, sort, limit, fields, statisticsFields));
  }

  private SelectOnConditionStep<Record> getProductStatusSelect(Filter filter, boolean isLatest,
      Sort sort, int limit,
      Collection<Field<?>> fields, Collection<String> contentFields) {
    return dsl.with(LAUNCHES)
        .as(QueryUtils.createQueryBuilderWithLatestLaunchesOption(filter, sort, isLatest).with(sort)
            .with(limit).build())
        .select(fields)
        .from(LAUNCH)
        .join(LAUNCHES)
        .on(LAUNCH.ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
        .leftJoin(dsl.select(STATISTICS.LAUNCH_ID, STATISTICS.S_COUNTER.as(STATISTICS_COUNTER),
                STATISTICS_FIELD.NAME.as(SF_NAME))
            .from(STATISTICS)
            .join(STATISTICS_FIELD)
            .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
            .where(STATISTICS_FIELD.NAME.in(contentFields))
            .asTable(STATISTICS_TABLE))
        .on(LAUNCH.ID.eq(fieldName(STATISTICS_TABLE, LAUNCH_ID).cast(Long.class)));
  }

  private ProductStatusStatisticsContent countLaunchTotalStatistics(
      List<ProductStatusStatisticsContent> launchesStatisticsResult) {
    Map<String, Integer> total = launchesStatisticsResult.stream()
        .flatMap(lsc -> lsc.getValues().entrySet().stream())
        .collect(Collectors.groupingBy(Map.Entry::getKey,
            summingInt(entry -> Integer.parseInt(entry.getValue()))));

    Double averagePassingRate = launchesStatisticsResult.stream()
        .collect(averagingDouble(lsc -> ofNullable(lsc.getPassingRate()).orElse(0D)));

    ProductStatusStatisticsContent launchesStatisticsContent = new ProductStatusStatisticsContent();
    launchesStatisticsContent.setTotalStatistics(total);

    Double roundedAveragePassingRate = BigDecimal.valueOf(averagePassingRate)
        .setScale(2, RoundingMode.HALF_UP).doubleValue();
    launchesStatisticsContent.setAveragePassingRate(roundedAveragePassingRate);

    return launchesStatisticsContent;
  }

  private List<ProductStatusStatisticsContent> countFilterTotalStatistics(
      Map<String, List<ProductStatusStatisticsContent>> launchesStatisticsResult) {
    Map<String, Integer> total = launchesStatisticsResult.values()
        .stream()
        .flatMap(Collection::stream)
        .flatMap(lsc -> lsc.getValues().entrySet().stream())
        .collect(Collectors.groupingBy(Map.Entry::getKey,
            summingInt(entry -> Integer.parseInt(entry.getValue()))));

    Double averagePassingRate = launchesStatisticsResult.values()
        .stream()
        .flatMap(Collection::stream)
        .collect(averagingDouble(lsc -> ofNullable(lsc.getPassingRate()).orElse(0D)));

    ProductStatusStatisticsContent launchesStatisticsContent = new ProductStatusStatisticsContent();
    launchesStatisticsContent.setTotalStatistics(total);

    Double roundedAveragePassingRate = BigDecimal.valueOf(averagePassingRate)
        .setScale(2, RoundingMode.HALF_UP).doubleValue();
    launchesStatisticsContent.setAveragePassingRate(roundedAveragePassingRate);

    return Lists.newArrayList(launchesStatisticsContent);
  }

  private List<TopPatternTemplatesContent> buildPatternTemplatesQuery(
      Map<String, List<Long>> attributeIdsMapping) {

    return attributeIdsMapping.entrySet()
        .stream()
        .map(entry -> (Select<? extends Record>) dsl.select(
                DSL.val(entry.getKey()).as(ATTRIBUTE_VALUE),
                PATTERN_TEMPLATE.NAME,
                DSL.countDistinct(PATTERN_TEMPLATE_TEST_ITEM.ITEM_ID).as(TOTAL)
            )
            .from(PATTERN_TEMPLATE)
            .join(PATTERN_TEMPLATE_TEST_ITEM)
            .on(PATTERN_TEMPLATE.ID.eq(PATTERN_TEMPLATE_TEST_ITEM.PATTERN_ID))
            .join(TEST_ITEM)
            .on(PATTERN_TEMPLATE_TEST_ITEM.ITEM_ID.eq(TEST_ITEM.ITEM_ID))
            .join(LAUNCH)
            .on(TEST_ITEM.LAUNCH_ID.eq(LAUNCH.ID))
            .where(LAUNCH.ID.in(entry.getValue()))
            .groupBy(PATTERN_TEMPLATE.NAME)
            .orderBy(field(TOTAL).desc())
            .limit(PATTERNS_COUNT))
        .reduce(Select::unionAll)
        .map(select -> TOP_PATTERN_TEMPLATES_FETCHER.apply(select.fetch()))
        .orElseGet(Collections::emptyList);
  }

  private List<TopPatternTemplatesContent> buildPatternTemplatesQueryGroupedByPattern(
      Map<String, List<Long>> attributeIdsMapping,
      String patternTemplateName) {

    return attributeIdsMapping.entrySet()
        .stream()
        .map(entry -> (Select<? extends Record>) dsl.select(
                DSL.val(entry.getKey()).as(ATTRIBUTE_VALUE),
                LAUNCH.ID,
                LAUNCH.NAME,
                LAUNCH.NUMBER,
                DSL.countDistinct(PATTERN_TEMPLATE_TEST_ITEM.ITEM_ID).as(TOTAL)
            )
            .from(PATTERN_TEMPLATE)
            .join(PATTERN_TEMPLATE_TEST_ITEM)
            .on(PATTERN_TEMPLATE.ID.eq(PATTERN_TEMPLATE_TEST_ITEM.PATTERN_ID))
            .join(TEST_ITEM)
            .on(PATTERN_TEMPLATE_TEST_ITEM.ITEM_ID.eq(TEST_ITEM.ITEM_ID))
            .join(LAUNCH)
            .on(TEST_ITEM.LAUNCH_ID.eq(LAUNCH.ID))
            .where(LAUNCH.ID.in(entry.getValue()))
            .and(PATTERN_TEMPLATE.NAME.eq(patternTemplateName))
            .groupBy(LAUNCH.ID, LAUNCH.NAME, LAUNCH.NUMBER, PATTERN_TEMPLATE.NAME)
            .having(DSL.countDistinct(PATTERN_TEMPLATE_TEST_ITEM.ITEM_ID)
                .gt(BigDecimal.ZERO.intValue()))
            .orderBy(field(TOTAL).desc()))
        .reduce(Select::unionAll)
        .map(select -> TOP_PATTERN_TEMPLATES_GROUPED_FETCHER.apply(select.fetch()))
        .orElseGet(Collections::emptyList);
  }

}
