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

package com.epam.reportportal.base.ws.converter.converters;

import com.epam.reportportal.base.infrastructure.persistence.entity.OwnedEntity;
import com.epam.reportportal.base.model.OwnedEntityResource;
import java.util.function.Function;

/**
 * Shared base mappings for JPA auditable or common column fields.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class BaseEntityConverter {

  public static final Function<? super OwnedEntity, OwnedEntityResource> TO_OWNED_ENTITY =
      shareable -> {
        OwnedEntityResource ownedEntity = new OwnedEntityResource();
        ownedEntity.setId(String.valueOf(shareable.getId()));
        ownedEntity.setOwner(shareable.getOwner());
        ownedEntity.setLocked(shareable.getLocked());
        return ownedEntity;
      };

  private BaseEntityConverter() {
    //static only
  }
}
