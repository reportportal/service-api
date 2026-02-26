package com.epam.reportportal.base.infrastructure.persistence.dao.widget.healthcheck.content.provider;

import static com.epam.reportportal.base.infrastructure.persistence.dao.util.WidgetContentUtil.COMPONENT_HEALTH_CHECK_TABLE_COLUMN_FETCHER;

import com.epam.reportportal.base.infrastructure.persistence.dao.widget.WidgetContentProvider;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck.HealthCheckTableGetParams;
import java.util.List;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class HealthCheckTableColumnProvider implements
    WidgetContentProvider<HealthCheckTableGetParams, Map<String, List<String>>> {

  private final DSLContext dslContext;

  @Autowired
  public HealthCheckTableColumnProvider(DSLContext dslContext) {
    this.dslContext = dslContext;
  }

  @Override
  public Map<String, List<String>> apply(Select<? extends Record> records, HealthCheckTableGetParams params) {
    return COMPONENT_HEALTH_CHECK_TABLE_COLUMN_FETCHER.apply(dslContext.fetch(records));
  }
}
