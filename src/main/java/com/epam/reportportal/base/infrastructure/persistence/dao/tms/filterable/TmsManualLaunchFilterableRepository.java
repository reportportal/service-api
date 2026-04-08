package com.epam.reportportal.base.infrastructure.persistence.dao.tms.filterable;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.ResultFetchers.LAUNCH_FETCHER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JLaunch.LAUNCH;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JTestItemResults.TEST_ITEM_RESULTS;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LaunchCriteriaConstant;
import com.epam.reportportal.base.infrastructure.persistence.dao.FilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JStatusEnum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

/**
 * Filterable repository for TMS Manual Launches.
 */
@Repository
public class TmsManualLaunchFilterableRepository implements FilterableRepository<Launch> {

  private static final String HAS_NOT_EXECUTED = "has_not_executed";
  private static final String DONE = "done";
  private static final String MANUAL = "MANUAL";
  private static final Table<Record> TEST_CASE_EXECUTION_TABLE = DSL.table(
      "tms_test_case_execution");
  private static final Field<Long> TEST_CASE_EXECUTION_TEST_ITEM_ID = DSL.field(
      DSL.name("tms_test_case_execution", "test_item_id"), Long.class);
  private static final Field<Long> TEST_CASE_EXECUTION_LAUNCH_ID = DSL.field(
      DSL.name("tms_test_case_execution", "launch_id"), Long.class);

  private final DSLContext dsl;

  public TmsManualLaunchFilterableRepository(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Override
  public List<Launch> findByFilter(Queryable filter) {
    var customConditions = new ArrayList<org.jooq.Condition>();
    extractAndApplyCustomConditions(filter, customConditions);
    var customJooqCondition =
        customConditions.isEmpty() ? DSL.noCondition() : DSL.and(customConditions);

    var fields = filter
        .getFilterConditions()
        .stream()
        .map(ConvertibleCondition::getAllConditions)
        .flatMap(Collection::stream)
        .map(FilterCondition::getSearchCriteria)
        .collect(Collectors.toSet());

    return LAUNCH_FETCHER.apply(
        dsl.fetch(QueryBuilder.newBuilder(filter, fields)
            .addCondition(customJooqCondition).wrap()
            .build())
    );
  }

  @Override
  public Page<Launch> findByFilter(Queryable filter, Pageable pageable) {
    var customConditions = new ArrayList<org.jooq.Condition>();
    extractAndApplyCustomConditions(filter, customConditions);
    var customJooqCondition =
        customConditions.isEmpty() ? DSL.noCondition() : DSL.and(customConditions);

    var fields = filter
        .getFilterConditions()
        .stream()
        .map(ConvertibleCondition::getAllConditions)
        .flatMap(Collection::stream)
        .map(FilterCondition::getSearchCriteria)
        .collect(Collectors.toSet());

    fields.addAll(pageable.getSort().get()
        .map(Sort.Order::getProperty)
        .collect(Collectors.toSet()));

    return PageableExecutionUtils.getPage(
        LAUNCH_FETCHER.apply(
            dsl.fetch(QueryBuilder.newBuilder(filter, fields)
                .addCondition(customJooqCondition)
                .with(pageable)
                .wrap()
                .withWrapperSort(pageable.getSort())
                .build())),
        pageable,
        () -> dsl.fetchCount(
            QueryBuilder.newBuilder(filter, fields).addCondition(customJooqCondition).build()));
  }

  /**
   * Finds manual launches by project ID and filter. Automatically adds filters for project ID and
   * MANUAL launch type.
   *
   * @param projectId project ID
   * @param filter    filter criteria
   * @param pageable  pagination parameters
   * @return page of manual launches
   */
  public Page<Launch> findByProjectIdAndFilter(Long projectId, Filter filter, Pageable pageable) {
    // Add project ID filter
    filter.withCondition(new FilterCondition(
        Condition.EQUALS,
        false,
        String.valueOf(projectId),
        CRITERIA_PROJECT_ID
    ));

    // Add MANUAL launch type filter
    filter.withCondition(new FilterCondition(
        Condition.EQUALS,
        false,
        MANUAL,
        CRITERIA_LAUNCH_TYPE
    ));

    return findByFilter(filter, pageable);
  }

  private void extractAndApplyCustomConditions(Queryable filter,
      List<org.jooq.Condition> customConditions) {
    if (!(filter instanceof Filter f)) {
      return;
    }

    var conditionsToRemove = new ArrayList<ConvertibleCondition>();

    for (var condition : f.getFilterConditions()) {
      if (condition instanceof FilterCondition filterCondition) {
        if (LaunchCriteriaConstant.CRITERIA_ITEM_STATUS.equals(
            filterCondition.getSearchCriteria())) {
          applyItemStatusFilter(filterCondition, customConditions);
          conditionsToRemove.add(condition);
        } else if (LaunchCriteriaConstant.CRITERIA_COMPLETION.equals(
            filterCondition.getSearchCriteria())) {
          applyCompletionFilter(filterCondition, customConditions);
          conditionsToRemove.add(condition);
        }
      }
    }
    f.getFilterConditions().removeAll(conditionsToRemove);
  }

  private void applyItemStatusFilter(
      FilterCondition filterCondition,
      List<org.jooq.Condition> customConditions) {
    var executionStatuses = Arrays
        .stream(filterCondition.getValue().split(","))
        .map(String::trim)
        .map(JStatusEnum::valueOf)
        .collect(Collectors.toList());

    customConditions.add(DSL.exists(
        getBaseExecutionSelect().and(TEST_ITEM_RESULTS.STATUS.in(executionStatuses))
    ));
  }

  private void applyCompletionFilter(FilterCondition filterCondition,
      List<org.jooq.Condition> customConditions) {
    var completionLogic = filterCondition.getValue();

    if (HAS_NOT_EXECUTED.equalsIgnoreCase(completionLogic)) {
      customConditions.add(DSL.exists(
          getBaseExecutionSelect().and(TEST_ITEM_RESULTS.STATUS.eq(JStatusEnum.TO_RUN))
      ));
    } else if (DONE.equalsIgnoreCase(completionLogic)) {
      customConditions.add(DSL.exists(
          DSL.selectOne().from(TEST_CASE_EXECUTION_TABLE)
              .where(TEST_CASE_EXECUTION_LAUNCH_ID.eq(LAUNCH.ID))
      ));
      customConditions.add(DSL.notExists(
          getBaseExecutionSelect().and(
              TEST_ITEM_RESULTS.STATUS.notIn(JStatusEnum.PASSED, JStatusEnum.FAILED))
      ));
    }
  }

  private SelectConditionStep<Record1<Integer>> getBaseExecutionSelect() {
    return DSL.selectOne()
        .from(TEST_CASE_EXECUTION_TABLE)
        .join(TEST_ITEM_RESULTS)
        .on(TEST_CASE_EXECUTION_TEST_ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
        .where(TEST_CASE_EXECUTION_LAUNCH_ID.eq(LAUNCH.ID));
  }
}
