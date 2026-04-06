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

package com.epam.reportportal.base.infrastructure.persistence.dao.organization;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUserId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
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

  /**
   * Finds all user IDs associated with a specific organization ID.
   *
   * @param orgId The ID of the organization.
   * @return A list of user IDs associated with the specified organization ID.
   */
  @Query(value = "SELECT ou.user_id FROM organization_user ou WHERE ou.organization_id = :orgId", nativeQuery = true)
  List<Long> findUserIdsByOrganizationId(@Param("orgId") Long orgId);

  /**
   * Deletes all entries from the organization_user table for the specified user ID and organization ID.
   *
   * @param userId The ID of the user whose associations should be deleted.
   * @param orgId  The ID of the organization whose user associations should be deleted.
   */
  @Modifying
  @Query(value =
      "DELETE FROM organization_user WHERE user_id = :userId AND organization_id = :orgId",
      nativeQuery = true)
  void deleteByUserIdAndOrganizationId(@Param("userId") Long userId, @Param("orgId") Long orgId);

  /**
   * Deletes all entries from the organization_user table for the specified organization ID, except for those with user
   * IDs in the provided list, and returns the deleted user IDs.
   *
   * @param orgId   The ID of the organization whose user associations should be deleted.
   * @param userIds The list of user IDs to retain.
   * @return List of user IDs that were removed
   */
  @Query(value =
      "DELETE FROM organization_user WHERE organization_id = :orgId AND user_id NOT IN (:userIds) RETURNING user_id",
      nativeQuery = true)
  List<Long> deleteByOrganizationIdAndUserIdNotIn(@Param("orgId") Long orgId, @Param("userIds") List<Long> userIds);


  /**
   * Deletes all entries from the organization_user table for the specified organization ID and returns the deleted user
   * IDs.
   *
   * @param orgId The ID of the organization whose user associations should be deleted.
   * @return List of user IDs that were removed
   */
  @Query(value = "DELETE FROM organization_user WHERE organization_id = :orgId RETURNING user_id", nativeQuery = true)
  List<Long> unassignAllUsersByOrgId(@Param(value = "orgId") Long orgId);
}
