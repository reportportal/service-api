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

package com.epam.reportportal.infrastructure.persistence.dao;

import com.epam.reportportal.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.infrastructure.persistence.entity.enums.PluginTypeEnum;
import com.epam.reportportal.infrastructure.persistence.entity.integration.IntegrationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link com.epam.reportportal.infrastructure.persistence.entity.integration.IntegrationType} entity
 *
 * @author Yauheni_Martynau
 */
public interface IntegrationTypeRepository extends ReportPortalRepository<IntegrationType, Long> {

  /**
   * Retrieve all {@link IntegrationType} by {@link IntegrationType#integrationGroup}
   *
   * @param integrationGroup {@link IntegrationType#integrationGroup}
   * @return The {@link List} of the {@link IntegrationType}
   */
  List<IntegrationType> findAllByIntegrationGroup(IntegrationGroupEnum integrationGroup);

  /**
   * Retrieve all {@link IntegrationType} ordered by {@link IntegrationType#creationDate} in ascending order
   *
   * @return The {@link List} of the {@link IntegrationType}
   */
  List<IntegrationType> findAllByOrderByCreationDate();

  /**
   * Find integration by name
   *
   * @param name Integration name
   * @return @return The {@link Optional} of the {@link IntegrationType}
   */
  Optional<IntegrationType> findByName(String name);

  /**
   * Retrieve all {@link IntegrationType} by accessType
   *
   * @param accessType {@link String}
   * @return The {@link List} of the {@link IntegrationType}
   */
  @Query(value = "SELECT it.* FROM integration_type it WHERE (it.details -> 'details'->>'accessType' = :accessType)", nativeQuery = true)
  List<IntegrationType> findAllByAccessType(@Param("accessType") String accessType);

  /**
   * Retrieve all {@link IntegrationType} by {@link PluginTypeEnum}
   *
   * @param pluginType {@link PluginTypeEnum}
   * @return The {@link List} of the {@link IntegrationType}
   */
  List<IntegrationType> findAllByPluginType(PluginTypeEnum pluginType);
}
