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
package com.epam.ta.reportportal.core.launch.repository;

import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence access for the {@code launches_modified} table, which records launches that need
 * downstream processing (for example indexing or synchronization).
 * <p>
 * Write operations use native SQL for an efficient upsert without loading {@link Launch} entities.
 */
public interface LaunchModifiedRepository extends JpaRepository<Launch, Long> {

  /**
   * Ensures a row exists for the given launch. If the launch is already present, refreshes
   * {@code created_at} so consumers can treat it as a new modification signal.
   *
   * @param launchId primary key of the launch to mark as modified
   */
  @Modifying
  @Query(value = """
      INSERT INTO launches_modified (launch_id)
      VALUES (:launchId)
      ON CONFLICT (launch_id) DO UPDATE SET created_at = clock_timestamp()
      """, nativeQuery = true)
  void insertIfAbsent(@Param("launchId") Long launchId);
}
