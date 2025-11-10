/*
 * Copyright 2020 EPAM Systems
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

package com.epam.reportportal.extension.event;

import java.util.Collection;
import org.springframework.context.ApplicationEvent;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class EntityCollectionEvent<E> extends ApplicationEvent {

  private final Collection<E> entities;

  public EntityCollectionEvent(Collection<E> entities) {
    super(entities);
    this.entities = entities;
  }

  public Collection<E> getEntities() {
    return entities;
  }

}
