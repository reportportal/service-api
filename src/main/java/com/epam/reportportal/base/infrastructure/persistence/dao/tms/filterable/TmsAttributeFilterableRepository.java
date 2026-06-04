package com.epam.reportportal.base.infrastructure.persistence.dao.tms.filterable;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.ResultFetchers.TMS_ATTRIBUTE_FETCHER;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.dao.FilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttribute;
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

@Repository
@RequiredArgsConstructor
public class TmsAttributeFilterableRepository implements FilterableRepository<TmsAttribute> {

  private final DSLContext dsl;

  @Override
  public List<TmsAttribute> findByFilter(Queryable filter) {
    Set<String> fields = filter.getFilterConditions()
        .stream()
        .map(ConvertibleCondition::getAllConditions)
        .flatMap(Collection::stream)
        .map(FilterCondition::getSearchCriteria)
        .collect(Collectors.toSet());

    return TMS_ATTRIBUTE_FETCHER.apply(
        dsl.fetch(QueryBuilder.newBuilder(filter, fields).wrap().build()));
  }

  @Override
  public Page<TmsAttribute> findByFilter(Queryable filter, Pageable pageable) {
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
        TMS_ATTRIBUTE_FETCHER.apply(
            dsl.fetch(QueryBuilder.newBuilder(filter, fields)
                .with(pageable)
                .wrap()
                .withWrapperSort(pageable.getSort())
                .build())),
        pageable,
        () -> dsl.fetchCount(QueryBuilder.newBuilder(filter, fields).build()));
  }

  public Page<TmsAttribute> findByProjectIdAndFilter(long projectId, Filter filter, Pageable pageable) {
    var filterWithProject = filter.withCondition(
        FilterCondition.builder()
            .eq(CRITERIA_PROJECT_ID, String.valueOf(projectId))
            .build()
    );
    return findByFilter(filterWithProject, pageable);
  }
}
