/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import com.epam.reportportal.base.infrastructure.persistence.entity.filter.UserFilter;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface WidgetRepositoryCustom extends FilterableRepository<Widget> {

  /**
   * Remove many to many relation between {@link UserFilter} by specified {@link UserFilter#id} and {@link Widget}
   * entities, that are not owned by the {@link UserFilter} owner
   *
   * @param filterId {@link UserFilter#id}
   * @param owner    {@link Widget#owner}
   * @return count of removed {@link Widget} entities
   */
  int deleteRelationByFilterIdAndNotOwner(Long filterId, String owner);

}
