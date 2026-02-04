package com.epam.reportportal.base.infrastructure.persistence.dao.widget;

import java.util.Set;
import java.util.function.Function;
import org.jooq.Record;
import org.jooq.Select;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface WidgetQueryProvider<T> extends Function<T, Select<? extends Record>> {

  Set<String> getSupportedSorting();
}
