package com.epam.reportportal.infrastructure.persistence.dao.tms.filterable;

import static com.epam.reportportal.infrastructure.persistence.dao.util.ResultFetchers.TMS_TEST_CASE_EXECUTION_FETCHER;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.TEST_ITEM_RESULTS;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.TMS_TEST_CASE_EXECUTION;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JTestItem.TEST_ITEM;

import com.epam.reportportal.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.QueryBuilder;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.infrastructure.persistence.dao.FilterableRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

/**
 * Filterable repository for TMS Test Case Execution with JOOQ support.
 */
@Repository
@RequiredArgsConstructor
public class TmsTestCaseExecutionFilterableRepository implements
    FilterableRepository<TmsTestCaseExecution> {

  private final DSLContext dsl;

  @Override
  public List<TmsTestCaseExecution> findByFilter(Queryable filter) {
    Set<String> fields = extractFields(filter, Sort.unsorted());

    return TMS_TEST_CASE_EXECUTION_FETCHER.apply(
        dsl.fetch(QueryBuilder.newBuilder(filter, fields).wrap().build())
    );
  }

  @Override
  public Page<TmsTestCaseExecution> findByFilter(Queryable filter, Pageable pageable) {
    Set<String> fields = extractFields(filter, pageable.getSort());

    return PageableExecutionUtils.getPage(
        TMS_TEST_CASE_EXECUTION_FETCHER.apply(
            dsl.fetch(QueryBuilder.newBuilder(filter, fields)
                .with(pageable)
                .wrap()
                .withWrapperSort(pageable.getSort())
                .build())
        ),
        pageable,
        () -> dsl.fetchCount(QueryBuilder.newBuilder(filter, fields).build())
    );
  }

  /**
   * Finds test case executions by launch ID with pagination and filtering.
   *
   * @param launchId launch ID
   * @param filter   filter criteria
   * @param pageable pagination parameters
   * @return page of test case executions
   */
  public Page<TmsTestCaseExecution> findByLaunchIdWithFilter(
      Long launchId,
      Filter filter,
      Pageable pageable) {

    // Add launch_id condition to filter
    filter.withCondition(new FilterCondition(
        Condition.EQUALS,
        false,
        String.valueOf(launchId),
        "launchId" // Criteria constant for launch_id field
    ));

    return findByFilter(filter, pageable);
  }

  /**
   * Finds test case executions by test case ID and launch ID.
   *
   * @param testCaseId test case ID
   * @param launchId   launch ID
   * @return list of executions
   */
  public List<TmsTestCaseExecution> findByTestCaseIdAndLaunchId(Long testCaseId, Long launchId) {
    var query = dsl.select(TMS_TEST_CASE_EXECUTION.fields())
        .select(TEST_ITEM.fields())
        .select(TEST_ITEM_RESULTS.fields())
        .from(TMS_TEST_CASE_EXECUTION)
        .leftJoin(TEST_ITEM).on(TEST_ITEM.ITEM_ID.eq(TMS_TEST_CASE_EXECUTION.TEST_ITEM_ID))
        .leftJoin(TEST_ITEM_RESULTS).on(TEST_ITEM_RESULTS.RESULT_ID.eq(TEST_ITEM.ITEM_ID))
        .where(TMS_TEST_CASE_EXECUTION.TEST_CASE_ID.eq(testCaseId))
        .and(TMS_TEST_CASE_EXECUTION.LAUNCH_ID.eq(launchId))
        .orderBy(TEST_ITEM.START_TIME.desc());

    return TMS_TEST_CASE_EXECUTION_FETCHER.apply(dsl.fetch(query));
  }

  /**
   * Extracts fields from filter conditions and sorting.
   *
   * @param filter filter
   * @param sort   sorting
   * @return set of field names
   */
  private Set<String> extractFields(Queryable filter, Sort sort) {
    Set<String> fields = filter.getFilterConditions()
        .stream()
        .map(ConvertibleCondition::getAllConditions)
        .flatMap(Collection::stream)
        .map(FilterCondition::getSearchCriteria)
        .collect(Collectors.toSet());

    fields.addAll(sort.get()
        .map(Sort.Order::getProperty)
        .collect(Collectors.toSet()));

    return fields;
  }
}
