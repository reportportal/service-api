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

package com.epam.reportportal.base.core.events.subscriber;

import com.epam.reportportal.base.core.events.domain.AbstractEvent;

/**
 * Synchronous callback for a published event type.
 *
 * @param <T> event type
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface EventSubscriber<T extends AbstractEvent<?>> {

  /**
   * Tells whether this subscriber has anything to do for the event. A listener may skip dispatching an event that no
   * subscriber applies to; {@link #handleEvent} must still tolerate an event this method rejects, since a listener can
   * dispatch to all subscribers once one applies. Must be cheap and in-memory only: no database queries, no HTTP calls
   * and no project config.
   *
   * @param event published event
   * @return {@code true} if the subscriber has work to do for the event
   */
  default boolean isApplicable(T event) {
    return true;
  }

  void handleEvent(T event);
}
