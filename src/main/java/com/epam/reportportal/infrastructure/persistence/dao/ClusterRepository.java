/*
 * Copyright 2021 EPAM Systems
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

import com.epam.reportportal.infrastructure.model.launch.cluster.ClusterInfoResource;
import com.epam.reportportal.infrastructure.persistence.entity.cluster.Cluster;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface ClusterRepository extends ReportPortalRepository<Cluster, Long>,
    ClusterRepositoryCustom {

  Optional<Cluster> findByIndexIdAndLaunchId(Long indexId, Long launchId);

  List<Cluster> findAllByLaunchId(Long launchId);

  Page<Cluster> findAllByLaunchId(Long launchId, Pageable pageable);

  int deleteAllByProjectId(Long projectId);

  int deleteAllByLaunchId(Long launchId);

  @Query(value =
      "SELECT new com.epam.reportportal.infrastructure.model.launch.cluster.ClusterInfoResource(c.id, c.indexId as index, c.launchId, c.message, count(cti.itemId) as matchedTests) "
          + "FROM Cluster c LEFT JOIN ClusterTestItem cti ON c.id = cti.clusterId "
          + "WHERE c.launchId = :launchId "
          + "GROUP BY c.id "
          + "HAVING count(cti.itemId) > 0")
  Page<ClusterInfoResource> findAllByLaunchIdWithCount(@Param("launchId") Long launchId, Pageable pageable);

  @Modifying
  @Query(value = "DELETE FROM clusters_test_item WHERE cluster_id IN (SELECT id FROM clusters WHERE project_id = :projectId)", nativeQuery = true)
  int deleteClusterTestItemsByProjectId(@Param("projectId") Long projectId);

  @Modifying
  @Query(value = "DELETE FROM clusters_test_item WHERE cluster_id IN (SELECT id FROM clusters WHERE launch_id = :launchId)", nativeQuery = true)
  int deleteClusterTestItemsByLaunchId(@Param("launchId") Long launchId);

  @Modifying
  @Query(value = "DELETE FROM clusters_test_item WHERE item_id = :itemId", nativeQuery = true)
  int deleteClusterTestItemsByItemId(@Param("itemId") Long itemId);

  @Modifying
  @Query(value = "DELETE FROM clusters_test_item WHERE item_id IN (:itemIds)", nativeQuery = true)
  int deleteClusterTestItemsByItemIds(@Param("itemIds") Collection<Long> itemIds);
}
