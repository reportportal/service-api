/*
 * Copyright 2026 EPAM Systems
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

import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.LaunchAttribute;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * {@link LaunchAttribute} rows stored in the launch-specific attributes table.
 */
public interface LaunchAttributeRepository extends ReportPortalRepository<LaunchAttribute, Long> {

  Optional<LaunchAttribute> findByLaunchIdAndKeyAndSystem(Long launchId, String key,
      boolean isSystem);

  void deleteAllByLaunchIdAndKeyAndSystem(Long launchId, String key, boolean isSystem);

  void deleteAllByKeyAndSystem(String key, boolean isSystem);

  @Modifying
  @Query("DELETE FROM LaunchAttribute la WHERE la.launch.id = :launchId AND la.system = :isSystem")
  void deleteAllByLaunchIdAndSystem(@Param("launchId") Long launchId,
      @Param("isSystem") boolean isSystem);

  @Query("""
      SELECT DISTINCT la.key
      FROM LaunchAttribute la
      WHERE la.launch.projectId = :projectId
        AND la.system = :system
        AND lower(la.key) LIKE lower(concat('%', :value, '%'))
      """)
  List<String> findLaunchAttributeKeys(@Param("projectId") Long projectId,
      @Param("value") String value,
      @Param("system") boolean system);

  @Query("""
      SELECT DISTINCT la.value
      FROM LaunchAttribute la
      WHERE la.launch.projectId = :projectId
        AND la.system = :system
        AND lower(la.value) LIKE lower(concat('%', :value, '%'))
        AND (:key IS NULL OR la.key = :key)
      """)
  List<String> findLaunchAttributeValues(@Param("projectId") Long projectId,
      @Param("key") String key,
      @Param("value") String value,
      @Param("system") boolean system);

  default LaunchAttribute saveByLaunchId(Long launchId, String key, String value,
      boolean isSystem) {
    LaunchAttribute attribute = new LaunchAttribute(key, value, isSystem);
    attribute.setLaunch(new Launch(launchId));
    save(attribute);
    return attribute;
  }
}
