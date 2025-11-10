package com.epam.reportportal.infrastructure.persistence.dao.widget;

import java.util.function.Function;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface WidgetProviderChain<T, R> extends Function<T, R> {

  default int resolvePriority(T input) {
    return 0;
  }
}
