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

import com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

/**
 * Access to key/value server instance settings rows.
 *
 * @author Ivan Budaev
 */
public interface ServerSettingsRepository extends ReportPortalRepository<ServerSettings, Long>,
    ServerSettingsRepositoryCustom {

  @Query(value = "INSERT INTO server_settings (key, value) VALUES ('secret.key', gen_random_bytes(32)) RETURNING value", nativeQuery = true)
  String generateSecret();

  Optional<ServerSettings> findByKey(String key);
}
