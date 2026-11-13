package com.epam.reportportal.base.infrastructure.persistence.dao.widget.healthcheck.query;

import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ITEM_ID;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.KEY;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.VALUE;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.JooqFieldNameTransformer.fieldName;

import com.epam.reportportal.base.infrastructure.persistence.dao.widget.WidgetQueryProvider;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck.HealthCheckTableGetParams;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.impl.DSL;

/**
 * Base jOOQ query builder for health check materialized views and drill-down levels.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractHealthCheckTableQueryProvider implements
    WidgetQueryProvider<HealthCheckTableGetParams> {

  private final Set<String> supportedSorting;

  /**
   * Stores sort keys supported by the concrete provider.
   *
   * @param supportedSorting allowed order-by keys for this provider
   */
  protected AbstractHealthCheckTableQueryProvider(Set<String> supportedSorting) {
    this.supportedSorting = supportedSorting;
  }

  /**
   * Builds the data {@link Select} for the health check view with the given level filters.
   *
   * @param params          request parameters
   * @param levelConditions drill-down level predicates
   * @return jOOQ select
   */
  protected abstract Select<? extends Record> contentQuery(HealthCheckTableGetParams params,
      List<Condition> levelConditions);

  @Override
  public Select<? extends Record> apply(HealthCheckTableGetParams params) {

    List<Condition> levelConditions = params.getPreviousLevels()
        .stream()
        .map(levelEntry -> fieldName(params.getViewName(), ITEM_ID).cast(Long.class)
            .in(DSL.selectDistinct(fieldName(ITEM_ID).cast(Long.class))
                .from(params.getViewName())
                .where(fieldName(KEY).cast(String.class)
                    .eq(levelEntry.getKey())
                    .and(fieldName(VALUE).cast(String.class).eq(levelEntry.getValue())))))
        .collect(Collectors.toList());

    return contentQuery(params, levelConditions);
  }

  @Override
  public Set<String> getSupportedSorting() {
    return supportedSorting;
  }
}
