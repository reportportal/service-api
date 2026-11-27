/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.core.events.handler;

import com.epam.reportportal.base.core.events.domain.AbstractEvent;

/**
 * Handles a domain event using handler-specific configuration.
 *
 * @param <T> event type
 * @param <C> config type
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface ConfigurableEventHandler<T extends AbstractEvent<?>, C> {

  /**
   * Tells whether this handler has anything to do for the event. The delegating subscriber calls it before it loads
   * the handler config and calls {@link #handle} only when it returns {@code true}.
   *
   * <p>Must be cheap and in-memory only: no database queries, no HTTP calls and no project config, because it runs
   * for every published event, before the project config is loaded.
   *
   * @param event published event
   * @return {@code true} if {@link #handle} should be called for the event
   */
  default boolean isApplicable(T event) {
    return true;
  }

  void handle(T event, C config);
}
