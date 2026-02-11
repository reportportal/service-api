package com.epam.reportportal.base.infrastructure.persistence.dao.widget.healthcheck.query;

import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.AGGREGATED_VALUES;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.CUSTOM_COLUMN;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.CUSTOM_COLUMN_SORTING;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.KEY;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.VALUE;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.JooqFieldNameTransformer.fieldName;

import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck.HealthCheckTableGetParams;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectHavingStep;
import org.jooq.SortOrder;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class CustomColumnQueryProvider extends AbstractHealthCheckTableQueryProvider {

  private final DSLContext dsl;

  public static final String UNNESTED_ARRAY = "unnested_array";

  public CustomColumnQueryProvider(DSLContext dslContext) {
    super(Sets.newHashSet(CUSTOM_COLUMN_SORTING));
    this.dsl = dslContext;
  }

  @Override
  protected Select<? extends Record> contentQuery(HealthCheckTableGetParams params,
      List<Condition> levelConditions) {
    SelectHavingStep<?> selectQuery = dsl.select(DSL.arrayAggDistinct(fieldName(UNNESTED_ARRAY))
            .filterWhere(fieldName(UNNESTED_ARRAY).isNotNull())
            .as(AGGREGATED_VALUES), fieldName(VALUE))
        .from(DSL.table(params.getViewName()),
            DSL.table(DSL.sql("unnest(?)", fieldName(CUSTOM_COLUMN))).as(UNNESTED_ARRAY))
        .where(fieldName(KEY).cast(String.class)
            .eq(params.getCurrentLevelKey())
            .and(levelConditions.stream().reduce(DSL.noCondition(), Condition::and)))
        .groupBy(fieldName(VALUE));

    Optional<Sort.Order> resolvedSort = params.getSort()
        .get()
        .filter(order -> getSupportedSorting().contains(order.getProperty()))
        .findFirst();

    if (resolvedSort.isPresent()) {
      return selectQuery.orderBy(resolvedSort.get().isAscending() ?
          fieldName(AGGREGATED_VALUES).sort(SortOrder.ASC) :
          fieldName(AGGREGATED_VALUES).sort(SortOrder.DESC));
    }
    return selectQuery;
  }
}
