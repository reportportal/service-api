package com.epam.reportportal.base.infrastructure.persistence.dao.widget;

import java.util.Set;
import java.util.function.Function;
import org.jooq.Record;
import org.jooq.Select;

/**
 * Builds a jOOQ {@code Select} for widget data from filter parameters of type {@code T}.
 *
 * @param <T> filter or request type (e.g. user filter, chart params)
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface WidgetQueryProvider<T> extends Function<T, Select<? extends Record>> {

  /**
   * Column or field names allowed in ORDER BY for this provider.
   *
   * @return supported sort field names
   */
  Set<String> getSupportedSorting();
}
