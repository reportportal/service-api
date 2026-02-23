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

package com.epam.ta.reportportal.core.statistics.repository;

import com.epam.ta.reportportal.dao.ReportPortalRepository;
import com.epam.ta.reportportal.entity.statistics.StatisticsField;
import java.util.Optional;

/**
 * Repository for StatisticsField operations in service-api.
 *
 * @author Pavel Bortnik
 */
public interface SStatisticsFieldRepository extends ReportPortalRepository<StatisticsField, Long> {

  /**
   * Finds statistics field by name.
   *
   * @param name field name
   * @return optional statistics field
   */
  Optional<StatisticsField> findByName(String name);
}
