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

import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import java.util.List;
import java.util.Optional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public interface ItemAttributeRepository extends ReportPortalRepository<ItemAttribute, Long>,
    ItemAttributeRepositoryCustom {

  Optional<ItemAttribute> findByLaunchIdAndKeyAndSystem(Long launchId, String key,
      boolean isSystem);

  int deleteAllByLaunchIdAndKeyAndSystem(Long launchId, String key, boolean isSystem);

  int deleteAllByKeyAndSystem(String key, boolean isSystem);

  int deleteAllByLaunchIdAndSystem(Long launchId, boolean isSystem);

  List<ItemAttribute> findAllByTestItem(TestItem testItem);
}
