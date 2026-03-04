package com.epam.reportportal.base.infrastructure.persistence.dao;

import static com.epam.reportportal.base.infrastructure.persistence.dao.util.ResultFetchers.TMS_TEST_FOLDER_TEST_ITEM_FETCHER;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolderTestItem;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TmsTestFolderTestItemFilterableRepository implements FilterableRepository<TmsTestFolderTestItem> {

  private final DSLContext dsl;

  @Override
  public List<TmsTestFolderTestItem> findByFilter(Queryable filter) {
    Set<String> fields = filter.getFilterConditions().stream()
        .map(ConvertibleCondition::getAllConditions)
        .flatMap(Collection::stream)
        .map(FilterCondition::getSearchCriteria)
        .collect(Collectors.toSet());
    return TMS_TEST_FOLDER_TEST_ITEM_FETCHER.apply(
        dsl.fetch(QueryBuilder.newBuilder(filter, fields).wrap().build())
    );
  }

  @Override
  public Page<TmsTestFolderTestItem> findByFilter(Queryable filter, Pageable pageable) {
    Set<String> fields = filter.getFilterConditions().stream()
        .map(ConvertibleCondition::getAllConditions)
        .flatMap(Collection::stream)
        .map(FilterCondition::getSearchCriteria)
        .collect(Collectors.toSet());
    pageable.getSort().forEach(order -> fields.add(order.getProperty()));

    return PageableExecutionUtils.getPage(
        TMS_TEST_FOLDER_TEST_ITEM_FETCHER.apply(
            dsl.fetch(
                QueryBuilder.newBuilder(filter, fields)
                    .with(pageable)
                    .wrap()
                    .withWrapperSort(pageable.getSort())
                    .build()
            )
        ),
        pageable,
        () -> dsl.fetchCount(QueryBuilder.newBuilder(filter, fields).build())
    );
  }
}
