/*
 * Copyright 2024 EPAM Systems
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

package com.epam.reportportal.infrastructure.persistence.dao.organization;

import com.epam.reportportal.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.infrastructure.persistence.entity.user.OrganizationUserId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * This interface represents a repository for the OrganizationUser entity.
 *
 * @author Siarhei Hrabko
 */
public interface OrganizationUserRepository extends
    ReportPortalRepository<OrganizationUser, OrganizationUserId> {

  /**
   * This method is used to find a list of OrganizationUser entities by user ID and organization ID. It returns a list
   * of OrganizationUser entities that match the given user ID and organization ID.
   *
   * @param userId The ID of the user.
   * @param orgId  The ID of the organization.
   * @return A list of OrganizationUser entities that match the given user ID and organization ID.
   */
  Optional<OrganizationUser> findByUserIdAndOrganization_Id(Long userId, Long orgId);


  /**
   * This method is used to find a list of organization IDs associated with a specific user ID. It executes a native SQL
   * query to retrieve the organization IDs.
   *
   * @param userId The ID of the user.
   * @return A list of organization IDs associated with the specified user ID.
   */
  @Query(value = "SELECT ou.organization_id FROM organization_user ou WHERE ou.user_id = :userId", nativeQuery = true)
  List<Long> findOrganizationIdsByUserId(@Param("userId") Long userId);

  @Query(value = """
      SELECT ou.organization_id FROM organization_user ou
      JOIN public.organization o ON o.id = ou.organization_id
      WHERE ou.user_id = :userId
      AND o.organization_type <> 'PERSONAL'
      """, nativeQuery = true)
  List<Long> findNonPersonalOrganizationIdsByUserId(@Param("userId") Long userId);
}
