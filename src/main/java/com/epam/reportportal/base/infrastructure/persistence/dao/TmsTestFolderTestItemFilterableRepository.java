package com.epam.reportportal.base.infrastructure.persistence.dao;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderTestItemCriteriaConstant.CHILD_ITEM;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.ResultFetchers.TMS_TEST_FOLDER_TEST_ITEM_FETCHER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_FOLDER_TEST_ITEM;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConditionType;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterTarget;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.query.JoinEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolderTestItem;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JTestItemTypeEnum;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Record2;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.TableLike;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TmsTestFolderTestItemFilterableRepository implements
    FilterableRepository<TmsTestFolderTestItem> {

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

  public Map<Long, Long> countTestCasesByFolderIdsAndFilter(List<Long> folderItemIds,
      Queryable filter) {
    if (folderItemIds == null || folderItemIds.isEmpty()) {
      return Collections.emptyMap();
    }

    var fields = filter.getFilterConditions()
        .stream()
        .map(ConvertibleCondition::getAllConditions)
        .flatMap(Collection::stream)
        .map(FilterCondition::getSearchCriteria)
        .collect(Collectors.toSet());

    var innerQuery = dsl
        .select(TMS_TEST_FOLDER_TEST_ITEM.TEST_ITEM_ID, CHILD_ITEM.ITEM_ID)
        .getQuery();

    innerQuery.addFrom(TMS_TEST_FOLDER_TEST_ITEM);

    innerQuery.addJoin(CHILD_ITEM, JoinType.LEFT_OUTER_JOIN,
        CHILD_ITEM.PARENT_ID.eq(TMS_TEST_FOLDER_TEST_ITEM.TEST_ITEM_ID)
            .and(CHILD_ITEM.TYPE.eq(JTestItemTypeEnum.TEST)));

    var filterTarget = filter.getTarget();
    var joinTables = new LinkedHashMap<TableLike<?>, JoinEntity>();
    fields.forEach(f -> filterTarget.getCriteriaByFilter(f).ifPresent(c -> {
      c.getJoinChain().forEach(j -> {
        if (!j.getTable().equals(CHILD_ITEM)) {
          joinTables.putIfAbsent(j.getTable(), j);
        }
      });
    }));

    joinTables.values().forEach(j ->
        innerQuery.addJoin(j.getTable(), j.getJoinType(), j.getJoinCondition())
    );

    var conditions = filter.toCondition();
    var whereCond = conditions.get(ConditionType.WHERE);
    var havingCond = conditions.get(ConditionType.HAVING);

    var finalWhere = TMS_TEST_FOLDER_TEST_ITEM.TEST_ITEM_ID.in(folderItemIds);
    if (whereCond != null) {
      finalWhere = finalWhere.and(whereCond);
    }
    innerQuery.addConditions(finalWhere);

    innerQuery.addGroupBy(TMS_TEST_FOLDER_TEST_ITEM.TEST_ITEM_ID, CHILD_ITEM.ITEM_ID);

    if (havingCond != null) {
      innerQuery.addHaving(havingCond);
    }

    var nested = innerQuery.asTable("nested");
    var folderIdField = nested.field(TMS_TEST_FOLDER_TEST_ITEM.TEST_ITEM_ID);
    var childIdField = nested.field(CHILD_ITEM.ITEM_ID);

    return dsl
        .select(folderIdField, DSL.count(childIdField).cast(Long.class))
        .from(nested)
        .groupBy(folderIdField)
        .fetchMap(folderIdField, DSL.count(childIdField).cast(Long.class));
  }
}
