package com.epam.reportportal.base.infrastructure.persistence.dao.widget;

import java.util.function.Function;

/**
 * Chainable widget data pipeline with optional per-provider priority.
 *
 * @param <T> input (e.g. request or filter)
 * @param <R> output (e.g. widget content or list)
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface WidgetProviderChain<T, R> extends Function<T, R> {

  default int resolvePriority(T input) {
    return 0;
  }
}
