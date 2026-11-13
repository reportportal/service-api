package com.epam.reportportal.base.infrastructure.persistence.dao.tms.filterable;

import static com.epam.reportportal.base.infrastructure.persistence.dao.util.ResultFetchers.TMS_TEST_FOLDER_FETCHER;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.dao.FilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
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

@Repository
public class TmsTestFolderFilterableRepository implements FilterableRepository<TmsTestFolder> {

  private final DSLContext dsl;

  public TmsTestFolderFilterableRepository(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Override
  public List<TmsTestFolder> findByFilter(Queryable filter) {
    Set<String> fields = filter.getFilterConditions()
        .stream()
        .map(ConvertibleCondition::getAllConditions)
        .flatMap(Collection::stream)
        .map(FilterCondition::getSearchCriteria)
        .collect(Collectors.toSet());

    return TMS_TEST_FOLDER_FETCHER.apply(
        dsl.fetch(QueryBuilder.newBuilder(filter, fields).wrap().build()));
  }

  @Override
  public Page<TmsTestFolder> findByFilter(Queryable filter, Pageable pageable) {
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
        TMS_TEST_FOLDER_FETCHER.apply(
            dsl.fetch(QueryBuilder.newBuilder(filter, fields)
                .with(pageable)
                .wrap()
                .withWrapperSort(pageable.getSort())
                .build())),
        pageable,
        () -> dsl.fetchCount(QueryBuilder.newBuilder(filter, fields).build()));
  }
}
