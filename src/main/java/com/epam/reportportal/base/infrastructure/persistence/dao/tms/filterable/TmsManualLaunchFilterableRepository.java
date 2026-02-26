package com.epam.reportportal.base.infrastructure.persistence.dao.tms.filterable;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.ResultFetchers.LAUNCH_FETCHER;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.dao.FilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
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

  private final DSLContext dsl;

  public TmsManualLaunchFilterableRepository(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Override
  public List<Launch> findByFilter(Queryable filter) {
    Set<String> fields = filter.getFilterConditions()
        .stream()
        .map(ConvertibleCondition::getAllConditions)
        .flatMap(Collection::stream)
        .map(FilterCondition::getSearchCriteria)
        .collect(Collectors.toSet());

    return LAUNCH_FETCHER.apply(
        dsl.fetch(QueryBuilder.newBuilder(filter, fields).wrap().build()));
  }

  @Override
  public Page<Launch> findByFilter(Queryable filter, Pageable pageable) {
    Set<String> fields = filter.getFilterConditions()
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
                .with(pageable)
                .wrap()
                .withWrapperSort(pageable.getSort())
                .build())),
        pageable,
        () -> dsl.fetchCount(QueryBuilder.newBuilder(filter, fields).build()));
  }

  /**
   * Finds manual launches by project ID and filter. Automatically adds filters for project ID and MANUAL launch type.
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
        "MANUAL",
        CRITERIA_LAUNCH_TYPE
    ));

    return findByFilter(filter, pageable);
  }
}
