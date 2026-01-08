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

import static com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterTarget.ATTRIBUTE_ALIAS;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterTarget.FILTERED_QUERY;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.ItemAttributeConstant.KEY_VALUE_SEPARATOR;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ID;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.LAUNCHES;
import static com.epam.reportportal.infrastructure.persistence.dao.util.JooqFieldNameTransformer.fieldName;
import static com.epam.reportportal.infrastructure.persistence.dao.util.RecordMappers.INDEX_LAUNCH_RECORD_MAPPER;
import static com.epam.reportportal.infrastructure.persistence.dao.util.ResultFetchers.LAUNCH_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.ITEM_ATTRIBUTE;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.LAUNCH;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.LOG;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.PROJECT;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.STATISTICS;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.STATISTICS_FIELD;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.USERS;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JTestItem.TEST_ITEM;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JTestItemResults.TEST_ITEM_RESULTS;
import static java.util.Optional.ofNullable;
import static org.jooq.impl.DSL.arrayAgg;
import static org.jooq.impl.DSL.arrayAggDistinct;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.concat;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.val;

import com.epam.reportportal.infrastructure.model.analyzer.IndexLaunch;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.QueryBuilder;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.infrastructure.persistence.dao.util.QueryUtils;
import com.epam.reportportal.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.jooq.enums.JLaunchModeEnum;
import com.epam.reportportal.infrastructure.persistence.jooq.enums.JStatusEnum;
import com.epam.reportportal.infrastructure.persistence.jooq.enums.JTestItemTypeEnum;
import com.epam.reportportal.infrastructure.persistence.util.SortUtils;
import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSON;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

/**
 * @author Pavel Bortnik
 */
@Repository
public class LaunchRepositoryCustomImpl implements LaunchRepositoryCustom {

  @Autowired
  private DSLContext dsl;

  @Override
  public boolean hasItemsInStatuses(Long launchId, List<JStatusEnum> statuses) {
    return dsl.fetchExists(dsl.selectOne()
        .from(TEST_ITEM)
        .join(TEST_ITEM_RESULTS)
        .on(TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .where(TEST_ITEM.LAUNCH_ID.eq(launchId).and(TEST_ITEM_RESULTS.STATUS.in(statuses))));
  }

  @Override
  public List<Launch> findByFilter(Queryable filter) {
    return LAUNCH_FETCHER.apply(dsl.fetch(QueryBuilder.newBuilder(filter,
        filter.getFilterConditions()
            .stream()
            .map(ConvertibleCondition::getAllConditions)
            .flatMap(Collection::stream)
            .map(FilterCondition::getSearchCriteria)
            .collect(Collectors.toSet())
    ).wrap().build()));
  }

  @Override
  public Page<Launch> findByFilter(Queryable filter, Pageable pageable) {
    Set<String> fields = filter.getFilterConditions()
        .stream()
        .map(ConvertibleCondition::getAllConditions)
        .flatMap(Collection::stream)
        .map(FilterCondition::getSearchCriteria)
        .collect(Collectors.toSet());
    fields.addAll(
        pageable.getSort().get().map(Sort.Order::getProperty).collect(Collectors.toSet()));

    return PageableExecutionUtils.getPage(
        LAUNCH_FETCHER.apply(dsl.fetch(QueryBuilder.newBuilder(filter, fields)
            .with(pageable)
            .wrap()
            .withWrapperSort(pageable.getSort())
            .build())), pageable,
        () -> dsl.fetchCount(QueryBuilder.newBuilder(filter, fields).build()));
  }

  @Override
  public List<String> getLaunchNamesByModeExcludedByStatus(Long projectId, String value,
      LaunchModeEnum mode, StatusEnum status) {
    return dsl.selectDistinct(LAUNCH.NAME)
        .from(LAUNCH)
        .leftJoin(PROJECT)
        .on(LAUNCH.PROJECT_ID.eq(PROJECT.ID))
        .where(PROJECT.ID.eq(projectId))
        .and(LAUNCH.MODE.eq(JLaunchModeEnum.valueOf(mode.name())))
        .and(LAUNCH.STATUS.notEqual(JStatusEnum.valueOf(status.name())))
        .and(LAUNCH.NAME.likeIgnoreCase("%" + DSL.escape(value, '\\') + "%"))
        .fetch(LAUNCH.NAME);
  }

  @Override
  public List<String> getOwnerNames(Long projectId, String value, String mode) {
    return dsl.selectDistinct(USERS.LOGIN)
        .from(LAUNCH)
        .leftJoin(PROJECT)
        .on(LAUNCH.PROJECT_ID.eq(PROJECT.ID))
        .leftJoin(USERS)
        .on(LAUNCH.USER_ID.eq(USERS.ID))
        .where(PROJECT.ID.eq(projectId))
        .and(USERS.LOGIN.likeIgnoreCase("%" + DSL.escape(value, '\\') + "%"))
        .and(LAUNCH.MODE.eq(JLaunchModeEnum.valueOf(mode)))
        .fetch(USERS.LOGIN);
  }

  @Override
  public Map<String, String> getStatuses(Long projectId, Long[] ids) {
    return dsl.select(LAUNCH.ID, LAUNCH.STATUS)
        .from(LAUNCH)
        .where(LAUNCH.PROJECT_ID.eq(projectId))
        .and(LAUNCH.ID.in(ids))
        .fetch()
        .intoMap(record -> String.valueOf(record.component1()),
            record -> record.component2().getLiteral());
  }

  @Override
  public Optional<Launch> findLatestByFilter(Filter filter) {
    return ofNullable(dsl.with(LAUNCHES)
        .as(QueryUtils.createQueryBuilderWithLatestLaunchesOption(filter, Sort.unsorted(), true)
            .build())
        .select()
        .from(LAUNCH)
        .join(LAUNCHES)
        .on(field(name(LAUNCHES, ID), Long.class).eq(LAUNCH.ID))
        .orderBy(LAUNCH.NAME, LAUNCH.NUMBER.desc())
        .fetchOneInto(Launch.class));
  }

  @Override
  public Page<Launch> findAllLatestByFilter(Queryable filter, Pageable pageable) {

    List<SortField<?>> sortFieldList = SortUtils.TO_SORT_FIELDS.apply(pageable.getSort(),
        filter.getTarget());
    List<Field<?>> simpleSelectedFields = getLaunchSimpleSelectedFields();

    List<Field<?>> selectFields = new ArrayList<>(simpleSelectedFields);
    selectFields.addAll(getAttributeConcatenatedFields());

    List<Field<?>> groupFields = new ArrayList<>(simpleSelectedFields);
    for (SortField<?> sortField : sortFieldList) {
      groupFields.add(DSL.field(sortField.getName()));
    }

    return PageableExecutionUtils.getPage(LAUNCH_FETCHER.apply(dsl.with(FILTERED_QUERY)
            .as(QueryUtils.createQueryBuilderWithLatestLaunchesOption(filter, pageable.getSort(), true)
                .with(pageable).build())
            .select(selectFields)
            .from(LAUNCH)
            .join(FILTERED_QUERY)
            .on(field(name(FILTERED_QUERY, ID), Long.class).eq(LAUNCH.ID))
            .leftJoin(STATISTICS)
            .on(LAUNCH.ID.eq(STATISTICS.LAUNCH_ID))
            .leftJoin(STATISTICS_FIELD)
            .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
            .leftJoin(ITEM_ATTRIBUTE)
            .on(LAUNCH.ID.eq(ITEM_ATTRIBUTE.LAUNCH_ID))
            .leftJoin(USERS)
            .on(LAUNCH.USER_ID.eq(USERS.ID))
            .groupBy(groupFields)
            .orderBy(sortFieldList)
            .fetch()),
        pageable,
        () -> dsl.fetchCount(dsl.with(LAUNCHES)
            .as(QueryUtils.createQueryBuilderWithLatestLaunchesOption(filter, pageable.getSort(),
                true).build())
            .selectOne()
            .distinctOn(LAUNCH.NAME)
            .from(LAUNCH)
            .join(LAUNCHES)
            .on(field(name(LAUNCHES, ID), Long.class).eq(LAUNCH.ID)))
    );
  }

  private ArrayList<Field<JSON[]>> getAttributeConcatenatedFields() {
    return Lists.newArrayList(
        DSL.arrayAgg(DSL.jsonArray(
                coalesce(ITEM_ATTRIBUTE.KEY, ""),
                coalesce(ITEM_ATTRIBUTE.VALUE, ""),
                ITEM_ATTRIBUTE.SYSTEM
            ))
            .as(ATTRIBUTE_ALIAS));
  }

  private ArrayList<Field<?>> getLaunchSimpleSelectedFields() {
    return Lists.newArrayList(LAUNCH.ID,
        LAUNCH.UUID,
        LAUNCH.NAME,
        LAUNCH.DESCRIPTION,
        LAUNCH.START_TIME,
        LAUNCH.END_TIME,
        LAUNCH.PROJECT_ID,
        LAUNCH.USER_ID,
        LAUNCH.NUMBER,
        LAUNCH.LAST_MODIFIED,
        LAUNCH.MODE,
        LAUNCH.STATUS,
        LAUNCH.HAS_RETRIES,
        LAUNCH.RERUN,
        LAUNCH.APPROXIMATE_DURATION,
        LAUNCH.RETENTION_POLICY,
        STATISTICS.S_COUNTER,
        STATISTICS_FIELD.NAME,
        USERS.ID,
        USERS.LOGIN);
  }

  @Override
  public List<Long> findLaunchIdsByProjectId(Long projectId) {
    return dsl.select(LAUNCH.ID).from(LAUNCH).where(LAUNCH.PROJECT_ID.eq(projectId))
        .fetchInto(Long.class);
  }

  @Override
  public Optional<Launch> findLastRun(Long projectId, String mode) {
    List<Field<?>> simpleSelectedFields = getLaunchSimpleSelectedFields();

    List<Field<?>> selectFields = new ArrayList<>(simpleSelectedFields);
    selectFields.addAll(getAttributeConcatenatedFields());

    return LAUNCH_FETCHER.apply(dsl.fetch(dsl.with(FILTERED_QUERY)
        .as(dsl.select(LAUNCH.ID)
            .from(LAUNCH)
            .where(LAUNCH.PROJECT_ID.eq(projectId)
                .and(LAUNCH.MODE.eq(JLaunchModeEnum.valueOf(mode))
                    .and(LAUNCH.STATUS.ne(JStatusEnum.IN_PROGRESS))))
            .orderBy(LAUNCH.START_TIME.desc())
            .limit(1))
        .select(selectFields)
        .from(LAUNCH)
        .join(FILTERED_QUERY)
        .on(LAUNCH.ID.eq(fieldName(FILTERED_QUERY, ID).cast(Long.class)))
        .leftJoin(STATISTICS)
        .on(LAUNCH.ID.eq(STATISTICS.LAUNCH_ID))
        .leftJoin(USERS)
        .on(LAUNCH.USER_ID.eq(USERS.ID))
        .leftJoin(STATISTICS_FIELD)
        .on(STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID))
        .leftJoin(ITEM_ATTRIBUTE)
        .on(LAUNCH.ID.eq(ITEM_ATTRIBUTE.LAUNCH_ID))
        .groupBy(simpleSelectedFields)
    )).stream().findFirst();
  }

  @Override
  public Integer countLaunches(Long projectId, String mode, Instant from) {
    return dsl.fetchCount(LAUNCH,
        LAUNCH.PROJECT_ID.eq(projectId)
            .and(LAUNCH.MODE.eq(JLaunchModeEnum.valueOf(mode)))
            .and(LAUNCH.STATUS.ne(JStatusEnum.IN_PROGRESS)
                .and(LAUNCH.START_TIME.greaterThan(from)))
    );
  }

  @Override
  public Integer countLaunches(Long projectId, String mode) {
    return dsl.fetchCount(LAUNCH,
        LAUNCH.PROJECT_ID.eq(projectId)
            .and(LAUNCH.MODE.eq(JLaunchModeEnum.valueOf(mode)))
            .and(LAUNCH.STATUS.ne(JStatusEnum.IN_PROGRESS))
    );
  }

  @Override
  public Map<String, Integer> countLaunchesGroupedByOwner(Long projectId, String mode,
      Instant from) {
    return dsl.select(USERS.LOGIN, DSL.count().as("count"))
        .from(LAUNCH)
        .join(USERS)
        .on(LAUNCH.USER_ID.eq(USERS.ID))
        .where(LAUNCH.PROJECT_ID.eq(projectId)
            .and(LAUNCH.MODE.eq(JLaunchModeEnum.valueOf(mode))
                .and(LAUNCH.STATUS.ne(JStatusEnum.IN_PROGRESS))
                .and(LAUNCH.START_TIME.greaterThan(from))))
        .groupBy(USERS.LOGIN)
        .fetchMap(USERS.LOGIN, field("count", Integer.class));
  }

  @Override
  public List<Long> findIdsByProjectIdAndModeAndStatusNotEq(Long projectId, JLaunchModeEnum mode,
      JStatusEnum status, int limit) {
    return dsl.select(LAUNCH.ID)
        .from(LAUNCH)
        .where(LAUNCH.PROJECT_ID.eq(projectId))
        .and(LAUNCH.MODE.eq(mode))
        .and(LAUNCH.STATUS.notEqual(status))
        .orderBy(LAUNCH.ID)
        .limit(limit)
        .fetchInto(Long.class);
  }

  @Override
  public List<Long> findIdsByProjectIdAndModeAndStatusNotEqAfterId(Long projectId,
      JLaunchModeEnum mode, JStatusEnum status,
      Long launchId, int limit) {
    return dsl.select(LAUNCH.ID)
        .from(LAUNCH)
        .where(LAUNCH.PROJECT_ID.eq(projectId))
        .and(LAUNCH.ID.gt(launchId))
        .and(LAUNCH.MODE.eq(mode))
        .and(LAUNCH.STATUS.notEqual(status))
        .orderBy(LAUNCH.ID)
        .limit(limit)
        .fetchInto(Long.class);
  }

  @Override
  public boolean hasItemsWithLogsWithLogLevel(Long launchId,
      Collection<JTestItemTypeEnum> itemTypes, Integer logLevel) {
    return dsl.fetchExists(dsl.selectOne()
        .from(TEST_ITEM)
        .join(LOG)
        .on(TEST_ITEM.ITEM_ID.eq(LOG.ITEM_ID))
        .where(TEST_ITEM.LAUNCH_ID.eq(launchId))
        .and(TEST_ITEM.TYPE.in(itemTypes))
        .and(LOG.LOG_LEVEL.ge(logLevel)));
  }

  @Override
  public List<IndexLaunch> findIndexLaunchByIds(List<Long> ids) {
    return dsl.select(LAUNCH.ID, LAUNCH.NAME, LAUNCH.PROJECT_ID, LAUNCH.START_TIME, LAUNCH.NUMBER)
        .from(LAUNCH)
        .where(LAUNCH.ID.in(ids))
        .orderBy(LAUNCH.ID)
        .fetch(INDEX_LAUNCH_RECORD_MAPPER);
  }

  @Override
  public Optional<Launch> findPreviousLaunchByProjectIdAndNameAndAttributesForLaunchIdAndModeNot(
      Long projectId, String name,
      String[] launchAttributes, Long launchId, JLaunchModeEnum mode) {
    return dsl.select()
        .from(LAUNCH)
        .where(LAUNCH.ID.in(dsl.select(LAUNCH.ID)
            .from(LAUNCH)
            .leftJoin(ITEM_ATTRIBUTE)
            .on(LAUNCH.ID.eq(ITEM_ATTRIBUTE.LAUNCH_ID))
            .where(ITEM_ATTRIBUTE.SYSTEM.eq(false))
            .and(LAUNCH.PROJECT_ID.eq(projectId))
            .and(LAUNCH.NAME.eq(name))
            .and(LAUNCH.ID.lt(launchId))
            .and(LAUNCH.MODE.ne(mode))
            .groupBy(LAUNCH.ID)
            .having(field("{0}::varchar[] || {1}::varchar[] || {2}::varchar[]",
                arrayAggDistinct(concat(coalesce(ITEM_ATTRIBUTE.KEY, ""))).filterWhere(
                    ITEM_ATTRIBUTE.SYSTEM.eq(false)),
                arrayAggDistinct(concat(KEY_VALUE_SEPARATOR, ITEM_ATTRIBUTE.VALUE)).filterWhere(
                    ITEM_ATTRIBUTE.SYSTEM.eq(
                        false)),
                arrayAgg(concat(coalesce(ITEM_ATTRIBUTE.KEY, ""),
                    val(KEY_VALUE_SEPARATOR),
                    ITEM_ATTRIBUTE.VALUE
                )).filterWhere(ITEM_ATTRIBUTE.SYSTEM.eq(false))
            ).contains(launchAttributes))))
        .orderBy(LAUNCH.NUMBER.desc())
        .limit(1)
        .fetchOptionalInto(Launch.class);
  }

  public Optional<Long> findPreviousLaunchId(Launch launch) {
    return dsl
        .select(LAUNCH.ID)
        .from(LAUNCH)
        .where(LAUNCH.ID.ne(launch.getId())
            .and(LAUNCH.NAME.eq(launch.getName()))
            .and(LAUNCH.NUMBER.lt(launch.getNumber().intValue())
                .and(LAUNCH.PROJECT_ID.eq(launch.getProjectId()))))
        .and(LAUNCH.MODE.ne(JLaunchModeEnum.DEBUG))
        .orderBy(LAUNCH.NUMBER.desc())
        .limit(1)
        .fetchOptionalInto(Long.class);
  }
}
