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

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterTarget.FILTERED_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterTarget.FILTERED_QUERY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder.retrieveOffsetAndApplyBoundaries;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_START_TIME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_MODE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_TEST_CASE_HASH;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_UNIQUE_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.RETRY_PARENT;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.LogRepositoryConstants.ITEM;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.TestItemRepositoryConstants.ATTACHMENTS_COUNT;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.TestItemRepositoryConstants.HAS_CONTENT;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.TestItemRepositoryConstants.NESTED;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.HISTORY;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ITEMS;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.LAUNCHES;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetRepositoryConstants.ID;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.JooqFieldNameTransformer.fieldName;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.QueryUtils.collectJoinFields;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.RecordMappers.INDEX_TEST_ITEM_RECORD_MAPPER;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.RecordMappers.ISSUE_TYPE_RECORD_MAPPER;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.RecordMappers.NESTED_STEP_RECORD_MAPPER;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.RecordMappers.TEST_ITEM_RECORD_MAPPER;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.ResultFetchers.TEST_ITEM_CLIPPED_FETCHER;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.ResultFetchers.TEST_ITEM_FETCHER;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.ResultFetchers.TEST_ITEM_RETRY_FETCHER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ATTACHMENT;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ISSUE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ISSUE_GROUP;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ISSUE_TYPE_PROJECT;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ITEM_ATTRIBUTE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.LAUNCH;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.LOG;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.PARAMETER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.PROJECT;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.STATISTICS;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.STATISTICS_FIELD;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JIssueType.ISSUE_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JTestItem.TEST_ITEM;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JTestItemResults.TEST_ITEM_RESULTS;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.with;

import com.epam.reportportal.base.infrastructure.model.analyzer.IndexTestItem;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.dao.util.QueryUtils;
import com.epam.reportportal.base.infrastructure.persistence.dao.util.TimestampUtils;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.ItemPathName;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.LaunchPathName;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.NestedStep;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.PathName;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.history.TestItemHistory;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.statistics.Statistics;
import com.epam.reportportal.base.infrastructure.persistence.entity.statistics.StatisticsField;
import com.epam.reportportal.base.infrastructure.persistence.jooq.Tables;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JIssueGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JLaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JStatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JTestItemTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JTestItem;
import com.google.common.collect.Lists;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.jooq.CommonTableExpression;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Log;
import org.jooq.Record;
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.SelectOnConditionStep;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

/**
 * @author Pavel Bortnik
 */
@Repository
@RequiredArgsConstructor
public class TestItemRepositoryCustomImpl implements TestItemRepositoryCustom {

  private static final String OUTER_ITEM_TABLE = "outer_item_table";
  private static final String INNER_ITEM_TABLE = "inner_item_table";
  private static final String TEST_CASE_ID_TABLE = "test_case_id_table";
  private static final String RESULT_OUTER_TABLE = "resultOuterTable";
  private static final String LATERAL_TABLE = "lateralTable";
  private static final String RESULT_INNER_TABLE = "resultInnerTable";

  private static final String CHILD_ITEM_TABLE = "child";

  private static final String BASELINE_TABLE = "baseline";

  private static final String ITEM_START_TIME = "itemStartTime";
  private static final String LAUNCH_START_TIME = "launchStartTime";
  private static final String ACCUMULATED_STATISTICS = "accumulated_statistics";

  private final DSLContext dsl;

  @Override
  public Set<Statistics> accumulateStatisticsByFilter(Queryable filter) {
    return dsl.fetch(with(FILTERED_QUERY)
        .as(QueryBuilder.newBuilder(filter, QueryUtils.collectJoinFields(filter)).build())
        .select(DSL.sum(STATISTICS.S_COUNTER).as(ACCUMULATED_STATISTICS), STATISTICS_FIELD.NAME)
        .from(STATISTICS)
        .join(DSL.table(name(FILTERED_QUERY)))
        .on(STATISTICS.ITEM_ID.eq(field(name(FILTERED_QUERY, FILTERED_ID), Long.class)))
        .join(STATISTICS_FIELD)
        .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
        .groupBy(STATISTICS_FIELD.NAME)
        .getQuery()).intoSet(r -> {
      Statistics statistics = new Statistics();
      StatisticsField statisticsField = new StatisticsField();
      statisticsField.setName(r.get(STATISTICS_FIELD.NAME));
      statistics.setStatisticsField(statisticsField);
      statistics.setCounter(ofNullable(r.get(ACCUMULATED_STATISTICS, Integer.class)).orElse(0));
      return statistics;
    });
  }

  @Override
  public Set<Statistics> accumulateStatisticsByFilterNotFromBaseline(Queryable targetFilter,
      Queryable baselineFilter) {
    final QueryBuilder targetBuilder =
        QueryBuilder.newBuilder(targetFilter,
            collectJoinFields(targetFilter));
    final QueryBuilder baselineBuilder =
        QueryBuilder.newBuilder(baselineFilter,
            collectJoinFields(baselineFilter));
    final SelectQuery<? extends Record> contentQuery =
        getQueryWithBaseline(targetBuilder,
            baselineBuilder).build();

    return dsl.fetch(with(FILTERED_QUERY)
        .as(contentQuery)
        .select(DSL.sum(STATISTICS.S_COUNTER).as(ACCUMULATED_STATISTICS), STATISTICS_FIELD.NAME)
        .from(STATISTICS)
        .join(DSL.table(name(FILTERED_QUERY)))
        .on(STATISTICS.ITEM_ID.eq(field(name(FILTERED_QUERY, FILTERED_ID), Long.class)))
        .join(STATISTICS_FIELD)
        .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
        .groupBy(STATISTICS_FIELD.NAME)
        .getQuery()).intoSet(r -> {
      Statistics statistics = new Statistics();
      StatisticsField statisticsField = new StatisticsField();
      statisticsField.setName(r.get(STATISTICS_FIELD.NAME));
      statistics.setStatisticsField(statisticsField);
      statistics.setCounter(ofNullable(r.get(ACCUMULATED_STATISTICS, Integer.class)).orElse(0));
      return statistics;
    });
  }

  @Override
  public Optional<Long> findIdByFilter(Queryable filter, Sort sort) {

    final Set<String> joinFields = QueryUtils.collectJoinFields(filter, sort);

    return dsl.select(fieldName(ID))
        .from(QueryBuilder.newBuilder(filter, joinFields).with(sort).with(1).build().asTable(ITEM))
        .fetchOptionalInto(Long.class);

  }

  @Override
  public Page<TestItem> findByFilter(boolean isLatest, Queryable launchFilter,
      Queryable testItemFilter, Pageable launchPageable,
      Pageable testItemPageable) {

    Table<? extends Record> launchesTable = QueryUtils.createQueryBuilderWithLatestLaunchesOption(
        launchFilter,
        launchPageable.getSort(),
        isLatest
    ).with(launchPageable).build().asTable(LAUNCHES);

    Set<String> joinFields =
        QueryUtils.collectJoinFields(testItemFilter,
            testItemPageable.getSort());

    return PageableExecutionUtils.getPage(
        TEST_ITEM_FETCHER.apply(dsl.fetch(QueryBuilder.newBuilder(testItemFilter, joinFields)
            .with(testItemPageable)
            .addJointToStart(launchesTable,
                JoinType.JOIN,
                TEST_ITEM.LAUNCH_ID.eq(fieldName(launchesTable.getName(), ID).cast(Long.class))
            )
            .wrap()
            .withWrapperSort(testItemPageable.getSort())
            .build())),
        testItemPageable,
        () -> dsl.fetchCount(QueryBuilder.newBuilder(testItemFilter, joinFields)
            .addJointToStart(launchesTable,
                JoinType.JOIN,
                TEST_ITEM.LAUNCH_ID.eq(fieldName(launchesTable.getName(), ID).cast(Long.class))
            )
            .build())
    );
  }

  @Override
  public Page<TestItem> findAllNotFromBaseline(Queryable targetFilter, Queryable baselineFilter,
      Pageable pageable) {

    final QueryBuilder targetBuilder = QueryBuilder.newBuilder(targetFilter,
        collectJoinFields(targetFilter, pageable.getSort()));
    final QueryBuilder baselineBuilder = QueryBuilder.newBuilder(baselineFilter,
        collectJoinFields(baselineFilter));
    final SelectQuery<? extends Record> contentQuery = getQueryWithBaseline(targetBuilder,
        baselineBuilder).with(pageable)
        .wrap()
        .withWrapperSort(pageable.getSort())
        .build();

    final QueryBuilder targetPagingBuilder =
        QueryBuilder.newBuilder(targetFilter,
            collectJoinFields(targetFilter, pageable.getSort()));
    final QueryBuilder baselinePagingBuilder =
        QueryBuilder.newBuilder(baselineFilter,
            collectJoinFields(baselineFilter));
    final SelectQuery<? extends Record> pagingQuery =
        getQueryWithBaseline(targetPagingBuilder,
            baselinePagingBuilder).build();

    return PageableExecutionUtils.getPage(TEST_ITEM_FETCHER.apply(dsl.fetch(contentQuery)),
        pageable,
        () -> dsl.fetchCount(pagingQuery)
    );

  }

  private QueryBuilder getQueryWithBaseline(QueryBuilder targetBuilder,
      QueryBuilder baselineBuilder) {

    final SelectQuery<? extends Record> baselineQuery = baselineBuilder
        .build();
    baselineQuery.addSelect(TEST_ITEM.TEST_CASE_HASH);
    final Table<? extends Record> baseline = baselineQuery.asTable(BASELINE_TABLE);

    //https://github.com/jOOQ/jOOQ/issues/11238
    final Condition baselineHashIsNull = condition(
        "array_agg(baseline.test_case_hash) filter (where baseline.test_case_hash is not null) is null");

    return targetBuilder
        .addJoinToEnd(baseline,
            JoinType.LEFT_OUTER_JOIN,
            TEST_ITEM.TEST_CASE_HASH.eq(
                fieldName(baseline.getName(), TEST_ITEM.TEST_CASE_HASH.getName()).cast(
                    Integer.class))
        )
        .addHavingCondition(baselineHashIsNull);
  }

  @Override
  public Page<TestItemHistory> loadItemsHistoryPage(Queryable filter, Pageable pageable,
      Long projectId, int historyDepth,
      boolean usingHash) {
    SelectQuery<? extends Record> filteringQuery = QueryBuilder.newBuilder(filter,
        QueryUtils.collectJoinFields(filter, pageable.getSort())
    ).with(pageable.getSort()).build();
    Field<?> historyGroupingField = usingHash ? TEST_ITEM.TEST_CASE_HASH : TEST_ITEM.UNIQUE_ID;
    Page<String> historyBaseline =
        loadHistoryBaseline(filteringQuery, historyGroupingField,
            LAUNCH.PROJECT_ID.eq(projectId),
            pageable);

    List<TestItemHistory> itemHistories = historyBaseline.getContent().stream().map(value -> {
      List<Long> itemIds = loadHistoryItem(getHistoryFilter(filter, usingHash, value),
          pageable.getSort(),
          LAUNCH.PROJECT_ID.eq(projectId)
      ).map(testItem -> getHistoryIds(testItem, usingHash, projectId, historyDepth - 1))
          .orElseGet(Collections::emptyList);
      return new TestItemHistory(value, itemIds);
    }).collect(Collectors.toList());

    return new PageImpl<>(itemHistories, pageable, historyBaseline.getTotalElements());

  }

  @Override
  public Page<TestItemHistory> loadItemsHistoryPage(Queryable filter, Pageable pageable,
      Long projectId, String launchName,
      int historyDepth, boolean usingHash) {
    SelectQuery<? extends Record> filteringQuery = QueryBuilder.newBuilder(filter,
        QueryUtils.collectJoinFields(filter, pageable.getSort())
    ).with(pageable.getSort()).build();
    Field<?> historyGroupingField = usingHash ? TEST_ITEM.TEST_CASE_HASH : TEST_ITEM.UNIQUE_ID;
    Page<String> historyBaseline = loadHistoryBaseline(filteringQuery,
        historyGroupingField,
        LAUNCH.PROJECT_ID.eq(projectId).and(LAUNCH.NAME.eq(launchName)),
        pageable
    );

    List<TestItemHistory> itemHistories = historyBaseline.getContent().stream().map(value -> {
      List<Long> itemIds = loadHistoryItem(getHistoryFilter(filter, usingHash, value),
          pageable.getSort(),
          LAUNCH.PROJECT_ID.eq(projectId).and(LAUNCH.NAME.eq(launchName))
      ).map(testItem -> getHistoryIds(testItem, usingHash, projectId, launchName, historyDepth - 1))
          .orElseGet(Collections::emptyList);
      return new TestItemHistory(value, itemIds);
    }).collect(Collectors.toList());

    return new PageImpl<>(itemHistories, pageable, historyBaseline.getTotalElements());

  }

  @Override
  public Page<TestItemHistory> loadItemsHistoryPage(Queryable filter, Pageable pageable,
      Long projectId, List<Long> launchIds,
      int historyDepth, boolean usingHash) {
    SelectQuery<? extends Record> filteringQuery = QueryBuilder.newBuilder(filter,
            QueryUtils.collectJoinFields(filter, pageable.getSort())
        )
        .with(pageable.getSort())
        .addCondition(LAUNCH.ID.in(launchIds).and(LAUNCH.PROJECT_ID.eq(projectId)))
        .build();

    Field<?> historyGroupingField = usingHash ? TEST_ITEM.TEST_CASE_HASH : TEST_ITEM.UNIQUE_ID;
    Page<String> historyBaseline = loadHistoryBaseline(filteringQuery,
        historyGroupingField,
        LAUNCH.ID.in(launchIds).and(LAUNCH.PROJECT_ID.eq(projectId)),
        pageable
    );

    List<TestItemHistory> itemHistories = historyBaseline.getContent().stream().map(value -> {
      List<Long> itemIds = loadHistoryItem(getHistoryFilter(filter, usingHash, value),
          pageable.getSort(),
          LAUNCH.ID.in(launchIds).and(LAUNCH.PROJECT_ID.eq(projectId))
      ).map(testItem -> getHistoryIds(testItem, usingHash, projectId, historyDepth - 1))
          .orElseGet(Collections::emptyList);
      return new TestItemHistory(value, itemIds);
    }).collect(Collectors.toList());

    return new PageImpl<>(itemHistories, pageable, historyBaseline.getTotalElements());

  }

  @Override
  public Page<TestItemHistory> loadItemsHistoryPage(boolean isLatest, Queryable launchFilter,
      Queryable testItemFilter,
      Pageable launchPageable, Pageable testItemPageable, Long projectId, int historyDepth,
      boolean usingHash) {
    SelectQuery<? extends Record> filteringQuery = buildCompositeFilterHistoryQuery(isLatest,
        launchFilter,
        testItemFilter,
        launchPageable,
        testItemPageable
    );

    Field<?> historyGroupingField = usingHash ? TEST_ITEM.TEST_CASE_HASH : TEST_ITEM.UNIQUE_ID;
    Page<String> historyBaseline = loadHistoryBaseline(filteringQuery,
        historyGroupingField,
        LAUNCH.PROJECT_ID.eq(projectId),
        testItemPageable
    );

    List<TestItemHistory> itemHistories = historyBaseline.getContent().stream().map(value -> {
      List<Long> itemIds = loadHistoryItem(getHistoryFilter(testItemFilter, usingHash, value),
          testItemPageable.getSort(),
          LAUNCH.PROJECT_ID.eq(projectId)
      ).map(testItem -> getHistoryIds(testItem, usingHash, projectId, historyDepth - 1))
          .orElseGet(Collections::emptyList);
      return new TestItemHistory(value, itemIds);
    }).collect(Collectors.toList());

    return new PageImpl<>(itemHistories, testItemPageable, historyBaseline.getTotalElements());
  }

  @Override
  public Page<TestItemHistory> loadItemsHistoryPage(boolean isLatest, Queryable launchFilter,
      Queryable testItemFilter,
      Pageable launchPageable, Pageable testItemPageable, Long projectId, String launchName,
      int historyDepth, boolean usingHash) {
    SelectQuery<? extends Record> filteringQuery = buildCompositeFilterHistoryQuery(isLatest,
        launchFilter,
        testItemFilter,
        launchPageable,
        testItemPageable
    );

    Field<?> historyGroupingField = usingHash ? TEST_ITEM.TEST_CASE_HASH : TEST_ITEM.UNIQUE_ID;
    Page<String> historyBaseline = loadHistoryBaseline(filteringQuery,
        historyGroupingField,
        LAUNCH.PROJECT_ID.eq(projectId).and(LAUNCH.NAME.eq(launchName)),
        testItemPageable
    );

    List<TestItemHistory> itemHistories = historyBaseline.getContent().stream().map(value -> {
      List<Long> itemIds = loadHistoryItem(getHistoryFilter(testItemFilter, usingHash, value),
          testItemPageable.getSort(),
          LAUNCH.PROJECT_ID.eq(projectId).and(LAUNCH.NAME.eq(launchName))
      ).map(testItem -> getHistoryIds(testItem, usingHash, projectId, historyDepth - 1))
          .orElseGet(Collections::emptyList);
      return new TestItemHistory(value, itemIds);
    }).collect(Collectors.toList());

    return new PageImpl<>(itemHistories, testItemPageable, historyBaseline.getTotalElements());
  }

  private SelectQuery<? extends Record> buildCompositeFilterHistoryQuery(boolean isLatest,
      Queryable launchFilter,
      Queryable testItemFilter, Pageable launchPageable, Pageable testItemPageable) {
    Table<? extends Record> launchesTable = QueryUtils.createQueryBuilderWithLatestLaunchesOption(
        launchFilter,
        launchPageable.getSort(),
        isLatest
    ).with(launchPageable).build().asTable(LAUNCHES);

    return QueryBuilder.newBuilder(testItemFilter,
            QueryUtils.collectJoinFields(testItemFilter, testItemPageable.getSort()))
        .with(testItemPageable.getSort())
        .addJointToStart(launchesTable,
            JoinType.JOIN,
            TEST_ITEM.LAUNCH_ID.eq(fieldName(launchesTable.getName(), ID).cast(Long.class))
        )
        .build();
  }

  private Page<String> loadHistoryBaseline(SelectQuery<? extends Record> filteringQuery,
      Field<?> historyGroupingField,
      Condition baselineCondition, Pageable pageable) {
    return PageableExecutionUtils.getPage(dsl.with(ITEMS)
            .as(filteringQuery)
            .select(historyGroupingField)
            .from(TEST_ITEM)
            .join(ITEMS)
            .on(TEST_ITEM.ITEM_ID.eq(fieldName(ITEMS, ID).cast(Long.class)))
            .join(LAUNCH)
            .on(TEST_ITEM.LAUNCH_ID.eq(LAUNCH.ID))
            .where(baselineCondition)
            .and(LAUNCH.MODE.eq(JLaunchModeEnum.DEFAULT))
            .groupBy(historyGroupingField)
            .orderBy(max(TEST_ITEM.START_TIME))
            .limit(pageable.getPageSize())
            .offset(retrieveOffsetAndApplyBoundaries(pageable))
            .fetchInto(String.class),
        pageable,
        () -> dsl.fetchCount(with(ITEMS).as(filteringQuery)
            .select(TEST_ITEM.field(historyGroupingField))
            .from(TEST_ITEM)
            .join(ITEMS)
            .on(TEST_ITEM.ITEM_ID.eq(fieldName(ITEMS, ID).cast(Long.class)))
            .groupBy(TEST_ITEM.field(historyGroupingField)))
    );
  }

  private Filter getHistoryFilter(Queryable filter, boolean usingHash, String historyValue) {
    List<ConvertibleCondition> commonConditions = filter.getFilterConditions();
    return new Filter(filter.getTarget().getClazz(), Lists.newArrayList()).withConditions(
            commonConditions)
        .withCondition(usingHash ?
            FilterCondition.builder().eq(CRITERIA_TEST_CASE_HASH, historyValue).build() :
            FilterCondition.builder().eq(CRITERIA_UNIQUE_ID, historyValue).build())
        .withCondition(
            FilterCondition.builder().eq(CRITERIA_LAUNCH_MODE, LaunchModeEnum.DEFAULT.name())
                .build());
  }

  private List<Long> getHistoryIds(TestItem testItem, boolean usingHash, Long projectId,
      int historyDepth) {
    List<Long> historyIds = usingHash ? loadHistory(testItem.getStartTime(),
        testItem.getItemId(),
        LAUNCH.PROJECT_ID.eq(projectId)
            .and(TEST_ITEM.TEST_CASE_HASH.eq(testItem.getTestCaseHash())),
        historyDepth
    ) : loadHistory(testItem.getStartTime(),
        testItem.getItemId(),
        LAUNCH.PROJECT_ID.eq(projectId).and(TEST_ITEM.UNIQUE_ID.eq(testItem.getUniqueId())),
        historyDepth
    );
    historyIds.add(0, testItem.getItemId());
    return historyIds;
  }

  private List<Long> getHistoryIds(TestItem testItem, boolean usingHash, Long projectId,
      String launchName, int historyDepth) {
    if (historyDepth > 0) {
      List<Long> historyIds = usingHash ? loadHistory(testItem.getStartTime(),
          testItem.getItemId(),
          LAUNCH.PROJECT_ID.eq(projectId)
              .and(LAUNCH.NAME.eq(launchName))
              .and(TEST_ITEM.TEST_CASE_HASH.eq(testItem.getTestCaseHash())),
          historyDepth
      ) : loadHistory(testItem.getStartTime(),
          testItem.getItemId(),
          LAUNCH.PROJECT_ID.eq(projectId).and(LAUNCH.NAME.eq(launchName))
              .and(TEST_ITEM.UNIQUE_ID.eq(testItem.getUniqueId())),
          historyDepth
      );
      historyIds.add(0, testItem.getItemId());
      return historyIds;
    }
    return Lists.newArrayList(testItem.getItemId());
  }

  private Optional<TestItem> loadHistoryItem(Queryable filter, Sort sort,
      Condition baselineCondition) {
    List<Sort.Order> orders = sort.get().collect(toList());
    orders.add(new Sort.Order(Sort.Direction.DESC, CRITERIA_START_TIME));

    SelectQuery<? extends Record> selectQuery = QueryBuilder.newBuilder(filter,
            QueryUtils.collectJoinFields(filter, sort))
        .with(Sort.by(orders))
        .with(1)
        .build();
    selectQuery.addConditions(baselineCondition);

    return dsl.with(HISTORY)
        .as(selectQuery)
        .select()
        .from(TEST_ITEM)
        .join(HISTORY)
        .on(TEST_ITEM.ITEM_ID.eq(fieldName(HISTORY, ID).cast(Long.class)))
        .fetchInto(TestItem.class)
        .stream()
        .findFirst();
  }

  private List<Long> loadHistory(Instant startTime, Long itemId, Condition baselineCondition,
      int historyDepth) {
    return dsl.select(TEST_ITEM.ITEM_ID)
        .from(TEST_ITEM)
        .join(LAUNCH)
        .on(TEST_ITEM.LAUNCH_ID.eq(LAUNCH.ID))
        .where(baselineCondition)
        .and(TEST_ITEM.ITEM_ID.notEqual(itemId))
        .and(TEST_ITEM.START_TIME.lessOrEqual(startTime))
        .and(LAUNCH.MODE.eq(JLaunchModeEnum.DEFAULT))
        .orderBy(TEST_ITEM.START_TIME.desc(), LAUNCH.START_TIME.desc(), LAUNCH.NUMBER.desc())
        .limit(historyDepth)
        .fetchInto(Long.class);
  }

  @Override
  public List<TestItem> selectAllDescendants(Long itemId) {
    return commonTestItemDslSelect().where(TEST_ITEM.PARENT_ID.eq(itemId))
        .fetch(TEST_ITEM_RECORD_MAPPER);
  }

  @Override
  public List<TestItem> selectAllDescendantsWithChildren(Long itemId) {
    JTestItem childTestItem = JTestItem.TEST_ITEM.as("cti");
    return commonTestItemDslSelect().where(TEST_ITEM.PARENT_ID.eq(itemId))
        .and(DSL.exists(dsl.selectOne()
            .from(TEST_ITEM)
            .join(childTestItem)
            .on(TEST_ITEM.ITEM_ID.eq(childTestItem.PARENT_ID))
            .where(TEST_ITEM.PARENT_ID.eq(itemId))))
        .fetch(TEST_ITEM_RECORD_MAPPER);
  }

  @Override
  public List<Long> findTestItemIdsByLaunchId(Long launchId, Pageable pageable) {
    JTestItem retryParent = TEST_ITEM.as(RETRY_PARENT);
    return dsl.select(TEST_ITEM.ITEM_ID)
        .from(TEST_ITEM)
        .leftJoin(retryParent)
        .on(TEST_ITEM.RETRY_OF.eq(retryParent.ITEM_ID))
        .where(TEST_ITEM.LAUNCH_ID.eq(launchId).or(retryParent.LAUNCH_ID.eq(launchId)))
        .orderBy(TEST_ITEM.ITEM_ID)
        .limit(pageable.getPageSize())
        .offset(retrieveOffsetAndApplyBoundaries(pageable))
        .fetchInto(Long.class);
  }

  @Override
  public List<TestItem> selectItemsInStatusByLaunch(Long launchId, StatusEnum... statuses) {
    List<JStatusEnum> jStatuses =
        Arrays.stream(statuses).map(it -> JStatusEnum.valueOf(it.name()))
            .collect(toList());
    return commonTestItemDslSelect().where(
            TEST_ITEM.LAUNCH_ID.eq(launchId).and(TEST_ITEM_RESULTS.STATUS.in(jStatuses)))
        .fetch(TEST_ITEM_RECORD_MAPPER);
  }

  @Override
  public List<TestItem> selectItemsInStatusByParent(Long itemId, StatusEnum... statuses) {
    List<JStatusEnum> jStatuses =
        Arrays.stream(statuses).map(it -> JStatusEnum.valueOf(it.name()))
            .collect(toList());
    return commonTestItemDslSelect().where(
            TEST_ITEM.PARENT_ID.eq(itemId).and(TEST_ITEM_RESULTS.STATUS.in(jStatuses)))
        .fetch(TEST_ITEM_RECORD_MAPPER);
  }

  @Override
  public Boolean hasItemsInStatusByLaunch(Long launchId, StatusEnum... statuses) {
    List<JStatusEnum> jStatuses =
        Arrays.stream(statuses).map(it -> JStatusEnum.valueOf(it.name()))
            .collect(toList());
    return dsl.fetchExists(dsl.selectOne()
        .from(TEST_ITEM)
        .join(TEST_ITEM_RESULTS)
        .onKey()
        .where(TEST_ITEM.LAUNCH_ID.eq(launchId))
        .and(TEST_ITEM_RESULTS.STATUS.in(jStatuses))
        .limit(1));
  }

  @Override
  public List<TestItem> findAllNotInIssueByLaunch(Long launchId, String locator) {
    return commonTestItemDslSelect().join(ISSUE)
        .on(ISSUE.ISSUE_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .join(ISSUE_TYPE)
        .on(ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID))
        .where(TEST_ITEM.LAUNCH_ID.eq(launchId))
        .and(ISSUE_TYPE.LOCATOR.ne(locator))
        .fetch(TEST_ITEM_RECORD_MAPPER);
  }

  @Override
  public List<Long> selectIdsNotInIssueByLaunch(Long launchId, String locator) {
    return dsl.select(TEST_ITEM.ITEM_ID)
        .from(TEST_ITEM)
        .join(TEST_ITEM_RESULTS)
        .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .join(ISSUE)
        .on(ISSUE.ISSUE_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .join(ISSUE_TYPE)
        .on(ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID))
        .where(TEST_ITEM.LAUNCH_ID.eq(launchId))
        .and(ISSUE_TYPE.LOCATOR.ne(locator))
        .fetchInto(Long.class);
  }

  @Override
  public List<TestItem> findAllNotInIssueGroupByLaunch(Long launchId,
      TestItemIssueGroup issueGroup) {
    return dsl.select()
        .from(TEST_ITEM)
        .join(TEST_ITEM_RESULTS)
        .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .join(ISSUE)
        .on(ISSUE.ISSUE_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .join(ISSUE_TYPE)
        .on(ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID))
        .join(ISSUE_GROUP)
        .on(ISSUE_TYPE.ISSUE_GROUP_ID.eq(ISSUE_GROUP.ISSUE_GROUP_ID))
        .where(TEST_ITEM.LAUNCH_ID.eq(launchId)
            .and(ISSUE_GROUP.ISSUE_GROUP_.ne(JIssueGroupEnum.valueOf(issueGroup.getValue()))))
        .fetch(TEST_ITEM_RECORD_MAPPER);

  }

  @Override
  public List<Long> selectIdsNotInIssueGroupByLaunch(Long launchId, TestItemIssueGroup issueGroup) {
    return dsl.select(TEST_ITEM.ITEM_ID)
        .from(TEST_ITEM)
        .join(TEST_ITEM_RESULTS)
        .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .join(ISSUE)
        .on(ISSUE.ISSUE_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .join(ISSUE_TYPE)
        .on(ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID))
        .join(ISSUE_GROUP)
        .on(ISSUE_TYPE.ISSUE_GROUP_ID.eq(ISSUE_GROUP.ISSUE_GROUP_ID))
        .where(TEST_ITEM.LAUNCH_ID.eq(launchId)
            .and(ISSUE_GROUP.ISSUE_GROUP_.ne(JIssueGroupEnum.valueOf(issueGroup.getValue()))))
        .fetchInto(Long.class);
  }

  @Override
  public List<TestItem> findAllInIssueGroupByLaunch(Long launchId, TestItemIssueGroup issueGroup) {
    return dsl.select()
        .from(TEST_ITEM)
        .join(TEST_ITEM_RESULTS)
        .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .join(ISSUE)
        .on(ISSUE.ISSUE_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .join(ISSUE_TYPE)
        .on(ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID))
        .join(ISSUE_GROUP)
        .on(ISSUE_TYPE.ISSUE_GROUP_ID.eq(ISSUE_GROUP.ISSUE_GROUP_ID))
        .where(TEST_ITEM.LAUNCH_ID.eq(launchId)
            .and(ISSUE_GROUP.ISSUE_GROUP_.eq(JIssueGroupEnum.valueOf(issueGroup.getValue()))))
        .fetch(TEST_ITEM_RECORD_MAPPER);
  }

  @Override
  public List<TestItem> findItemsForAnalyze(Long launchId) {
    return dsl.select()
        .from(TEST_ITEM)
        .join(TEST_ITEM_RESULTS)
        .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .join(ISSUE)
        .on(ISSUE.ISSUE_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .join(ISSUE_TYPE)
        .on(ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID))
        .join(ISSUE_GROUP)
        .on(ISSUE_TYPE.ISSUE_GROUP_ID.eq(ISSUE_GROUP.ISSUE_GROUP_ID))
        .leftOuterJoin(ITEM_ATTRIBUTE)
        .on(TEST_ITEM.ITEM_ID.eq(ITEM_ATTRIBUTE.ITEM_ID)
            .and(ITEM_ATTRIBUTE.KEY.eq("immediateAutoAnalysis"))
            .and(ITEM_ATTRIBUTE.VALUE.eq("true"))
            .and(ITEM_ATTRIBUTE.SYSTEM.eq(true)))
        .where(TEST_ITEM.LAUNCH_ID.eq(launchId)
            .and(ISSUE_GROUP.ISSUE_GROUP_.eq(
                JIssueGroupEnum.valueOf(TestItemIssueGroup.TO_INVESTIGATE.getValue()))))
        .and(ITEM_ATTRIBUTE.ID.isNull())
        .fetch(TEST_ITEM_RECORD_MAPPER);
  }

  @Override
  public List<TestItem> selectTestItemsProjection(Long launchId) {
    return TEST_ITEM_CLIPPED_FETCHER.apply(dsl.fetch(dsl.select()
        .from(TEST_ITEM)
        .join(TEST_ITEM_RESULTS)
        .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .leftJoin(ATTACHMENT)
        .on(TEST_ITEM.ITEM_ID.eq(ATTACHMENT.ITEM_ID))
        .leftJoin(STATISTICS)
        .on(TEST_ITEM.ITEM_ID.eq(STATISTICS.ITEM_ID))
        .leftJoin(STATISTICS_FIELD)
        .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
        .where(TEST_ITEM.LAUNCH_ID.eq(launchId)).and(TEST_ITEM.HAS_STATS.eq(true))
        .orderBy(TEST_ITEM.START_TIME.asc())));
  }

  @Override
  public List<Long> selectIdsWithIssueByLaunch(Long launchId) {
    return dsl.select(TEST_ITEM.ITEM_ID)
        .from(TEST_ITEM)
        .join(TEST_ITEM_RESULTS)
        .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .join(ISSUE)
        .on(ISSUE.ISSUE_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .where(TEST_ITEM.LAUNCH_ID.eq(launchId))
        .fetchInto(Long.class);
  }

  @Override
  public Boolean hasItemsInStatusAddedLately(Long launchId, Duration period,
      StatusEnum... statuses) {
    List<JStatusEnum> jStatuses =
        Arrays.stream(statuses).map(it -> JStatusEnum.valueOf(it.name()))
            .collect(toList());
    return dsl.fetchExists(dsl.selectOne()
        .from(TEST_ITEM)
        .join(TEST_ITEM_RESULTS)
        .onKey()
        .where(TEST_ITEM.LAUNCH_ID.eq(launchId))
        .and(TEST_ITEM_RESULTS.STATUS.in(jStatuses))
        .and(TEST_ITEM.START_TIME.gt(TimestampUtils.getInstantBackFromNow(period)))
        .limit(1));
  }

  @Override
  public Boolean hasLogs(Long launchId, Duration period, StatusEnum... statuses) {
    List<JStatusEnum> jStatuses =
        Arrays.stream(statuses).map(it -> JStatusEnum.valueOf(it.name()))
            .collect(toList());
    return dsl.fetchExists(dsl.selectOne()
        .from(TEST_ITEM)
        .join(TEST_ITEM_RESULTS)
        .onKey()
        .join(LOG)
        .on(TEST_ITEM.ITEM_ID.eq(LOG.ITEM_ID))
        .where(TEST_ITEM.LAUNCH_ID.eq(launchId))
        .and(TEST_ITEM_RESULTS.STATUS.in(jStatuses))
        .and(TEST_ITEM.START_TIME.lt(TimestampUtils.getInstantBackFromNow(period)))
        .limit(1));
  }

  @Override
  public List<TestItem> selectItemsInIssueByLaunch(Long launchId, String issueType) {
    return commonTestItemDslSelect().join(ISSUE)
        .on(ISSUE.ISSUE_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .join(ISSUE_TYPE)
        .on(ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID))
        .where(TEST_ITEM.LAUNCH_ID.eq(launchId))
        .and(ISSUE_TYPE.LOCATOR.eq(issueType))
        .fetch(TEST_ITEM_RECORD_MAPPER::map);
  }

  @Override
  public List<TestItem> selectRetries(List<Long> retryOfIds) {
    return TEST_ITEM_RETRY_FETCHER.apply(dsl.select()
        .from(TEST_ITEM)
        .join(TEST_ITEM_RESULTS)
        .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .leftJoin(PARAMETER)
        .on(TEST_ITEM.ITEM_ID.eq(PARAMETER.ITEM_ID))
        .where(TEST_ITEM.RETRY_OF.in(retryOfIds))
        .and(TEST_ITEM.LAUNCH_ID.isNull())
        .orderBy(TEST_ITEM.START_TIME)
        .fetch());
  }

  @Override
  public List<IssueType> selectIssueLocatorsByProject(Long projectId) {
    return dsl.select()
        .from(PROJECT)
        .join(ISSUE_TYPE_PROJECT)
        .on(PROJECT.ID.eq(ISSUE_TYPE_PROJECT.PROJECT_ID))
        .join(ISSUE_TYPE)
        .on(ISSUE_TYPE_PROJECT.ISSUE_TYPE_ID.eq(ISSUE_TYPE.ID))
        .join(ISSUE_GROUP)
        .on(Tables.ISSUE_TYPE.ISSUE_GROUP_ID.eq(ISSUE_GROUP.ISSUE_GROUP_ID))
        .where(PROJECT.ID.eq(projectId))
        .fetch(ISSUE_TYPE_RECORD_MAPPER);
  }

  @Override
  public Optional<IssueType> selectIssueTypeByLocator(Long projectId, String locator) {
    return ofNullable(dsl.select()
        .from(ISSUE_TYPE)
        .join(ISSUE_TYPE_PROJECT)
        .on(ISSUE_TYPE_PROJECT.ISSUE_TYPE_ID.eq(ISSUE_TYPE.ID))
        .join(ISSUE_GROUP)
        .on(ISSUE_TYPE.ISSUE_GROUP_ID.eq(ISSUE_GROUP.ISSUE_GROUP_ID))
        .where(ISSUE_TYPE_PROJECT.PROJECT_ID.eq(projectId))
        .and(ISSUE_TYPE.LOCATOR.eq(locator))
        .fetchOne(ISSUE_TYPE_RECORD_MAPPER));
  }

  @Override
  public Optional<Pair<Long, String>> selectPath(String uuid) {
    return dsl.select(TEST_ITEM.ITEM_ID, TEST_ITEM.PATH)
        .from(TEST_ITEM)
        .where(TEST_ITEM.UUID.eq(uuid))
        .fetchOptional(r -> Pair.of(r.get(TEST_ITEM.ITEM_ID), r.get(TEST_ITEM.PATH, String.class)));
  }

  /**
   * {@link Log} entities are searched from the whole tree under {@link TestItem} that matched to the provided
   * `launchId` and `autoAnalyzed` conditions
   */
  @Override
  public List<Long> selectIdsByAnalyzedWithLevelGteExcludingIssueTypes(boolean autoAnalyzed,
      boolean ignoreAnalyzer,
      Long launchId,
      int logLevel, Collection<IssueType> excludedIssueTypes) {

    JTestItem outerItemTable = TEST_ITEM.as(OUTER_ITEM_TABLE);
    JTestItem nestedItemTable = TEST_ITEM.as(NESTED);

    final List<Long> excludedTypeIds = excludedIssueTypes.stream().map(IssueType::getId)
        .collect(toList());
    final Condition issueCondition = ISSUE.AUTO_ANALYZED.eq(autoAnalyzed)
        .and(ISSUE.IGNORE_ANALYZER.eq(ignoreAnalyzer))
        .and(ISSUE.ISSUE_TYPE.notIn(excludedTypeIds));

    return dsl.selectDistinct(fieldName(ID))
        .from(dsl.select(outerItemTable.ITEM_ID.as(ID))
            .from(outerItemTable)
            .join(TEST_ITEM_RESULTS)
            .on(outerItemTable.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
            .join(ISSUE)
            .on(TEST_ITEM_RESULTS.RESULT_ID.eq(ISSUE.ISSUE_ID))
            .where(outerItemTable.LAUNCH_ID.eq(launchId))
            .and(outerItemTable.HAS_STATS)
            .andNot(outerItemTable.HAS_CHILDREN)
            .and(issueCondition)
            .and(DSL.exists(dsl.selectOne()
                .from(nestedItemTable)
                .join(LOG)
                .on(nestedItemTable.ITEM_ID.eq(LOG.ITEM_ID))
                .where(nestedItemTable.LAUNCH_ID.eq(launchId))
                .andNot(nestedItemTable.HAS_STATS)
                .and(LOG.LOG_LEVEL.greaterOrEqual(logLevel))
                .and(DSL.sql(outerItemTable.PATH + " @> " + nestedItemTable.PATH))))
            .unionAll(dsl.selectDistinct(TEST_ITEM.ITEM_ID.as(ID))
                .from(TEST_ITEM)
                .join(TEST_ITEM_RESULTS)
                .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
                .join(ISSUE)
                .on(TEST_ITEM_RESULTS.RESULT_ID.eq(ISSUE.ISSUE_ID))
                .join(LOG)
                .on(TEST_ITEM.ITEM_ID.eq(LOG.ITEM_ID))
                .where(TEST_ITEM.LAUNCH_ID.eq(launchId))
                .and(issueCondition)
                .and(LOG.LOG_LEVEL.greaterOrEqual(logLevel)))
            .asTable(ITEM))
        .fetchInto(Long.class);
  }

  @Override
  public int updateStatusAndEndTimeById(Long itemId, JStatusEnum status, Instant endTime) {

    return dsl.update(TEST_ITEM_RESULTS)
        .set(TEST_ITEM_RESULTS.STATUS, status)
        .set(TEST_ITEM_RESULTS.END_TIME, endTime)
        .set(TEST_ITEM_RESULTS.DURATION,
            dsl.select(DSL.extract(endTime, DatePart.EPOCH)
                .minus(DSL.extract(TEST_ITEM.START_TIME, DatePart.EPOCH))
                .cast(Double.class)).from(TEST_ITEM).where(TEST_ITEM.ITEM_ID.eq(itemId))
        )
        .where(TEST_ITEM_RESULTS.RESULT_ID.eq(itemId))
        .execute();
  }

  @Override
  public int updateStatusAndEndTimeByRetryOfId(Long retryOfId, JStatusEnum from, JStatusEnum to,
      Instant endTime) {
    return dsl.update(TEST_ITEM_RESULTS)
        .set(TEST_ITEM_RESULTS.STATUS, to)
        .set(TEST_ITEM_RESULTS.END_TIME, endTime)
        .set(TEST_ITEM_RESULTS.DURATION,
            dsl.select(DSL.extract(endTime, DatePart.EPOCH)
                    .minus(DSL.extract(TEST_ITEM.START_TIME, DatePart.EPOCH))
                    .cast(Double.class)).from(TEST_ITEM)
                .where(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        )
        .where(TEST_ITEM_RESULTS.RESULT_ID.in(dsl.select(TEST_ITEM.ITEM_ID)
            .from(TEST_ITEM)
            .where(TEST_ITEM.RETRY_OF.eq(retryOfId))))
        .and(TEST_ITEM_RESULTS.STATUS.eq(from))
        .execute();
  }

  @Override
  public TestItemTypeEnum getTypeByItemId(Long itemId) {
    return dsl.select(TEST_ITEM.TYPE).from(TEST_ITEM).where(TEST_ITEM.ITEM_ID.eq(itemId))
        .fetchOneInto(TestItemTypeEnum.class);
  }

  @Override
  public List<Long> selectIdsByFilter(Long launchId, Queryable filter, int limit, int offset) {
    final SelectQuery<? extends Record> selectQuery = QueryBuilder.newBuilder(filter,
            QueryUtils.collectJoinFields(filter))
        .with(limit)
        .withOffset(offset)
        .with(Sort.by(Sort.Order.asc(CRITERIA_ID)))
        .build();
    selectQuery.addConditions(TEST_ITEM.LAUNCH_ID.eq(launchId));
    return dsl.select(fieldName(ITEMS, ID)).from(selectQuery.asTable(ITEMS)).fetchInto(Long.class);
  }

  @Override
  public List<Long> selectIdsByHasDescendants(Collection<Long> itemIds) {
    final JTestItem parent = TEST_ITEM.as(OUTER_ITEM_TABLE);
    final JTestItem child = TEST_ITEM.as(CHILD_ITEM_TABLE);
    return dsl.select(parent.ITEM_ID)
        .from(parent)
        .join(child)
        .on(parent.ITEM_ID.eq(child.PARENT_ID))
        .where(parent.ITEM_ID.in(itemIds))
        .groupBy(parent.ITEM_ID)
        .fetchInto(Long.class);
  }

  @Override
  public List<Long> selectIdsByStringLogMessage(Collection<Long> itemIds, Integer logLevel,
      String pattern) {
    return dsl.selectDistinct(TEST_ITEM.ITEM_ID)
        .from(TEST_ITEM)
        .join(LOG)
        .on(TEST_ITEM.ITEM_ID.eq(LOG.ITEM_ID))
        .where(TEST_ITEM.ITEM_ID.in(itemIds))
        .and(LOG.LOG_LEVEL.greaterOrEqual(logLevel))
        .and(LOG.LOG_MESSAGE.like("%" + DSL.escape(pattern, '\\') + "%"))
        .fetchInto(Long.class);
  }

  @Override
  public List<Long> selectIdsByRegexLogMessage(Collection<Long> itemIds, Integer logLevel,
      String pattern) {
    return dsl.selectDistinct(TEST_ITEM.ITEM_ID)
        .from(TEST_ITEM)
        .join(LOG)
        .on(TEST_ITEM.ITEM_ID.eq(LOG.ITEM_ID))
        .where(TEST_ITEM.ITEM_ID.in(itemIds))
        .and(LOG.LOG_LEVEL.greaterOrEqual(logLevel))
        .and(LOG.LOG_MESSAGE.likeRegex(pattern))
        .fetchInto(Long.class);
  }

  @Override
  public List<Long> selectLogIdsWithLogLevelCondition(Collection<Long> itemIds, Integer logLevel) {
    return dsl.selectDistinct(LOG.ID)
        .from(TEST_ITEM)
        .join(LOG)
        .on(TEST_ITEM.ITEM_ID.eq(LOG.ITEM_ID))
        .where(TEST_ITEM.ITEM_ID.in(itemIds))
        .and(LOG.LOG_LEVEL.greaterOrEqual(logLevel))
        .fetchInto(Long.class);
  }

  @Override
  public List<Long> selectIdsUnderByStringLogMessage(Long launchId, Collection<Long> itemIds,
      Integer logLevel, String pattern) {
    final JTestItem child = TEST_ITEM.as(CHILD_ITEM_TABLE);

    return dsl.selectDistinct(TEST_ITEM.ITEM_ID)
        .from(TEST_ITEM)
        .join(child)
        .on(TEST_ITEM.PATH + " @> " + child.PATH)
        .and(TEST_ITEM.ITEM_ID.notEqual(child.ITEM_ID))
        .join(LOG)
        .on(child.ITEM_ID.eq(LOG.ITEM_ID))
        .where(TEST_ITEM.ITEM_ID.in(itemIds))
        .and(child.LAUNCH_ID.eq(launchId))
        .and(LOG.LOG_LEVEL.greaterOrEqual(logLevel))
        .and(LOG.LOG_MESSAGE.like("%" + DSL.escape(pattern, '\\') + "%"))
        .groupBy(TEST_ITEM.ITEM_ID)
        .fetchInto(Long.class);
  }

  @Override
  public List<Long> selectLogIdsUnderWithLogLevelCondition(Long launchId, Collection<Long> itemIds,
      Integer logLevel) {
    final JTestItem child = TEST_ITEM.as(CHILD_ITEM_TABLE);

    return dsl.selectDistinct(LOG.ID)
        .from(TEST_ITEM)
        .join(child)
        .on(TEST_ITEM.PATH + " @> " + child.PATH)
        .and(TEST_ITEM.ITEM_ID.notEqual(child.ITEM_ID))
        .join(LOG)
        .on(child.ITEM_ID.eq(LOG.ITEM_ID))
        .where(TEST_ITEM.ITEM_ID.in(itemIds))
        .and(child.LAUNCH_ID.eq(launchId))
        .and(LOG.LOG_LEVEL.greaterOrEqual(logLevel))
        .fetchInto(Long.class);
  }

  @Override
  public List<Long> selectIdsUnderByRegexLogMessage(Long launchId, Collection<Long> itemIds,
      Integer logLevel, String pattern) {
    final JTestItem child = TEST_ITEM.as(CHILD_ITEM_TABLE);

    return dsl.selectDistinct(TEST_ITEM.ITEM_ID)
        .from(TEST_ITEM)
        .join(child)
        .on(TEST_ITEM.PATH + " @> " + child.PATH)
        .and(TEST_ITEM.ITEM_ID.notEqual(child.ITEM_ID))
        .join(LOG)
        .on(child.ITEM_ID.eq(LOG.ITEM_ID))
        .where(TEST_ITEM.ITEM_ID.in(itemIds))
        .and(child.LAUNCH_ID.eq(launchId))
        .and(LOG.LOG_LEVEL.greaterOrEqual(logLevel))
        .and(LOG.LOG_MESSAGE.likeRegex(pattern))
        .groupBy(TEST_ITEM.ITEM_ID)
        .fetchInto(Long.class);
  }

  /**
   * Commons select of an item with it's results and structure
   *
   * @return Select condition step
   */
  private SelectOnConditionStep<Record> commonTestItemDslSelect() {
    return dsl.select().from(TEST_ITEM).join(TEST_ITEM_RESULTS)
        .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID));
  }

  @Override
  public List<TestItem> findByFilter(Queryable filter) {
    return TEST_ITEM_FETCHER.apply(dsl.fetch(
        QueryBuilder.newBuilder(filter, QueryUtils.collectJoinFields(filter)).wrap().build()));
  }

  @Override
  public Page<TestItem> findByFilter(Queryable filter, Pageable pageable) {

    Set<String> joinFields = QueryUtils.collectJoinFields(filter, pageable.getSort());
    List<TestItem> items =
        TEST_ITEM_FETCHER.apply(
            dsl.fetch(QueryBuilder.newBuilder(filter, joinFields)
                .with(pageable)
                .wrap()
                .withWrapperSort(pageable.getSort())
                .build()));

    return PageableExecutionUtils.getPage(items, pageable,
        () -> dsl.fetchCount(QueryBuilder.newBuilder(filter, joinFields).build()));
  }

  @Override
  public List<NestedStep> findAllNestedStepsByIds(Collection<Long> ids, Queryable logFilter,
      boolean excludePassedLogs) {
    JTestItem nested = TEST_ITEM.as(NESTED);

    CommonTableExpression<?> logsCte = name("logsCTE")
        .as(QueryBuilder.newBuilder(logFilter, QueryUtils.collectJoinFields(logFilter)).build());

    return dsl.with(logsCte).select(TEST_ITEM.ITEM_ID,
            TEST_ITEM.NAME,
            TEST_ITEM.UUID,
            TEST_ITEM.START_TIME,
            TEST_ITEM.TYPE,
            TEST_ITEM_RESULTS.STATUS,
            TEST_ITEM_RESULTS.END_TIME,
            TEST_ITEM_RESULTS.DURATION,
            field(hasContentQuery(nested, logsCte, excludePassedLogs)).as(HAS_CONTENT),
            field(dsl.selectCount()
                .from(LOG)
                .join(nested)
                .on(LOG.ITEM_ID.eq(nested.ITEM_ID))
                .join(logsCte)
                .on(LOG.ID.eq(logsCte.field(ID).cast(Long.class)))
                .join(ATTACHMENT)
                .on(LOG.ATTACHMENT_ID.eq(ATTACHMENT.ID))
                .where(nested.HAS_STATS.isFalse()
                    .and(DSL.sql(fieldName(NESTED, TEST_ITEM.PATH.getName()) + " <@ cast(? AS LTREE)",
                        TEST_ITEM.PATH))))
                .as(ATTACHMENTS_COUNT)
        )
        .from(TEST_ITEM)
        .join(TEST_ITEM_RESULTS)
        .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .where(TEST_ITEM.ITEM_ID.in(ids))
        .fetch(NESTED_STEP_RECORD_MAPPER);
  }

  @Override
  public List<IndexTestItem> findIndexTestItemByLaunchId(Long launchId,
      Collection<JTestItemTypeEnum> itemTypes) {
    return dsl.select(TEST_ITEM.ITEM_ID,
            TEST_ITEM.NAME,
            TEST_ITEM.START_TIME,
            TEST_ITEM.UNIQUE_ID,
            TEST_ITEM.TEST_CASE_HASH,
            ISSUE.AUTO_ANALYZED,
            ISSUE_TYPE.LOCATOR
        )
        .from(TEST_ITEM)
        .join(TEST_ITEM_RESULTS)
        .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .join(ISSUE)
        .on(TEST_ITEM_RESULTS.RESULT_ID.eq(ISSUE.ISSUE_ID))
        .join(ISSUE_TYPE)
        .on(ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID))
        .where(TEST_ITEM.LAUNCH_ID.eq(launchId))
        .and(TEST_ITEM.TYPE.in(itemTypes))
        .and(ISSUE.IGNORE_ANALYZER.isFalse())
        .fetch(INDEX_TEST_ITEM_RECORD_MAPPER);

  }

  private Condition hasContentQuery(JTestItem nested, CommonTableExpression logsCte,
      boolean excludePassedLogs) {
    if (excludePassedLogs) {
      return DSL.exists(dsl.select()
              .from(LOG)
              .join(logsCte)
              .on(LOG.ID.eq(logsCte.field(ID).cast(Long.class)))
              .join(TEST_ITEM_RESULTS)
              .on(LOG.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
              .where(LOG.ITEM_ID.eq(TEST_ITEM.ITEM_ID)))
          .and(TEST_ITEM_RESULTS.STATUS.notIn(JStatusEnum.PASSED, JStatusEnum.INFO,
              JStatusEnum.WARN));
    } else {
      return DSL.exists(dsl.select()
              .from(LOG)
              .join(logsCte)
              .on(LOG.ID.eq(logsCte.field(ID).cast(Long.class)))
              .where(LOG.ITEM_ID.eq(TEST_ITEM.ITEM_ID)))
          .orExists(dsl.select().from(nested)
              .where(nested.PARENT_ID.eq(TEST_ITEM.ITEM_ID).and(nested.HAS_STATS.isFalse())));
    }
  }

  /**
   * Selects and returns a map of PathName objects based on the provided collection of TestItem objects.
   *
   * @param testItems the collection of TestItem objects to be used to generate the map of PathNames
   * @return a map with the test item IDs as keys and the PathName objects as values. If the provided collection of test
   * items is null or empty, it returns an empty map.
   */
  @Override
  public Map<Long, PathName> selectPathNames(Collection<TestItem> testItems) {
    if (CollectionUtils.isEmpty(testItems)) {
      return new HashMap<>();
    }

    // Item ids for search
    Set<Long> testItemIds = new HashSet<>();
    // Structure for creating return object
    Map<Long, List<Long>> testItemWithPathIds = new HashMap<>();

    for (TestItem testItem : testItems) {
      String path = testItem.getPath();
      // For normal case is redundant, but not sure for current situation, better to check
      // and skip testItem without path, cause of incorrect state.
      if (Strings.isBlank(path)) {
        continue;
      }
      String[] pathIds = path.split("\\.");
      Arrays.asList(pathIds).forEach(
          pathItemId -> {
            long itemIdFromPath = Long.parseLong(pathItemId);
            testItemIds.add(itemIdFromPath);

            List<Long> itemPaths =
                testItemWithPathIds.getOrDefault(testItem.getItemId(),
                    new ArrayList<>());
            itemPaths.add(itemIdFromPath);
            testItemWithPathIds.put(testItem.getItemId(), itemPaths);
          }
      );
    }

    Map<Long, Record4<Long, String, Integer, String>> resultMap = new HashMap<>();
    // Convert database data to more useful form
    getTestItemAndLaunchIdName(testItemIds).forEach(record -> resultMap.put(
        record.get(fieldName("item_id"), Long.class), record
    ));

    Map<Long, PathName> testItemPathNames = new HashMap<>();
    testItemWithPathIds.forEach((testItemId, pathIds) -> {
      var record = resultMap.get(testItemId);
      if (record == null) {
        return;
      }

      LaunchPathName launchPathName = new LaunchPathName(
          record.get(fieldName("launch_name"), String.class),
          record.get(fieldName("number"), Integer.class)
      );

      List<ItemPathName> itemPathNames = new ArrayList<>();
      pathIds.forEach(pathItemId -> {
        // Base testItem don't add
        if (!testItemId.equals(pathItemId) && resultMap.containsKey(pathItemId)) {
          var record2 = resultMap.get(pathItemId);
          String pathItemName = record2.get(fieldName("name"), String.class);
          itemPathNames.add(new ItemPathName(pathItemId, pathItemName));
        }
      });
      PathName pathName = new PathName(launchPathName, itemPathNames);
      testItemPathNames.put(testItemId, pathName);
    });

    return testItemPathNames;
  }

  /**
   * @param testItemIds Collection<Long>
   * @return Result<Record4 < testItemId, testItemName, launchNumber, launchName>>
   */
  private Result<Record4<Long, String, Integer, String>> getTestItemAndLaunchIdName(
      Collection<Long> testItemIds) {
    return dsl.select(TEST_ITEM.ITEM_ID, TEST_ITEM.NAME, LAUNCH.NUMBER,
            LAUNCH.NAME.as("launch_name"))
        .from(TEST_ITEM)
        .join(LAUNCH)
        .on(TEST_ITEM.LAUNCH_ID.eq(LAUNCH.ID))
        .where(TEST_ITEM.ITEM_ID.in(testItemIds))
        .fetch();
  }
}
