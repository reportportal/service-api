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

import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.infrastructure.model.launch.cluster.ClusterInfoResource;
import com.epam.reportportal.ws.BaseMvcTest;
import com.epam.reportportal.infrastructure.persistence.entity.cluster.Cluster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Sql("/db/fill/launch/launch-fill.sql")
class ClusterRepositoryTest extends BaseMvcTest {

  private static final long CLUSTER_ID_START_VALUE = 1L;
  private static final long CLUSTER_ID_END_VALUE = 4L;
  private static final long PROJECT_ID = 1L;
  private static final long LAUNCH_ID = 1L;

  private final List<Long> savedIds = new ArrayList<>();

  @Autowired
  private ClusterRepository clusterRepository;

  @BeforeEach
  void insertClusters() {
    final List<Cluster> clusters = LongStream.range(CLUSTER_ID_START_VALUE, CLUSTER_ID_END_VALUE)
        .mapToObj(id -> {
          final Cluster cluster = new Cluster();
          cluster.setIndexId(id);
          cluster.setProjectId(PROJECT_ID);
          cluster.setLaunchId(LAUNCH_ID);
          cluster.setMessage("Message");
          return cluster;
        }).collect(Collectors.toList());
    clusterRepository.saveAll(clusters);
    clusterRepository.saveClusterTestItems(clusters.get(0), Set.of(1L));
    clusters.stream().map(Cluster::getId).forEach(savedIds::add);
  }

  @AfterEach
  void removeClusters() {
    savedIds.stream().map(clusterRepository::findById)
        .forEach(c -> c.ifPresent(clusterRepository::delete));
    savedIds.clear();
  }

  @Test
  void shouldFindAllByLaunchId() {
    final List<Cluster> clusters = clusterRepository.findAllByLaunchId(LAUNCH_ID);
    assertFalse(clusters.isEmpty());
    assertEquals(3, clusters.size());
    clusters.forEach(cluster -> assertEquals(LAUNCH_ID, cluster.getLaunchId()));
  }

  @Test
  void shouldFindAllByLaunchIdWithCount() {
    final Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Order.by(CRITERIA_ID)));
    final Page<ClusterInfoResource> clusters = clusterRepository.findAllByLaunchIdWithCount(LAUNCH_ID, pageable);
    assertFalse(clusters.isEmpty());
    assertEquals(1, clusters.getContent().size());
    clusters.getContent().forEach(cluster -> assertEquals(LAUNCH_ID, cluster.getLaunchId()));
  }

  @Test
  void shouldFindByLaunchId() {

    final Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Order.by(CRITERIA_ID)));
    final Page<Cluster> clusters = clusterRepository.findAllByLaunchId(LAUNCH_ID, pageable);
    assertFalse(clusters.isEmpty());
    assertEquals(3, clusters.getContent().size());
    clusters.getContent().forEach(cluster -> assertEquals(LAUNCH_ID, cluster.getLaunchId()));
  }

  @Test
  void shouldDeleteByProjectId() {
    final int removed = clusterRepository.deleteAllByProjectId(PROJECT_ID);
    assertEquals(3, removed);

    final Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Order.by(CRITERIA_ID)));
    final Page<Cluster> clusters = clusterRepository.findAllByLaunchId(LAUNCH_ID, pageable);
    assertTrue(clusters.isEmpty());
  }

  @Test
  void shouldDeleteByLaunchId() {
    final int removed = clusterRepository.deleteAllByLaunchId(LAUNCH_ID);
    assertEquals(3, removed);

    final Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Order.by(CRITERIA_ID)));
    final Page<Cluster> clusters = clusterRepository.findAllByLaunchId(LAUNCH_ID, pageable);
    assertTrue(clusters.isEmpty());
  }

  @Test
  void shouldDeleteClusterTestItemsByProjectId() {
    final int removed = clusterRepository.deleteClusterTestItemsByProjectId(1L);
    assertEquals(1, removed);
  }

  @Test
  void shouldDeleteClusterTestItemsByLaunchId() {
    final int removed = clusterRepository.deleteClusterTestItemsByLaunchId(1L);
    assertEquals(1, removed);
  }

  @Test
  void shouldDeleteClusterTestItemsByItemId() {
    final int removed = clusterRepository.deleteClusterTestItemsByItemId(1L);
    assertEquals(1, removed);
  }

  @Test
  void shouldDeleteClusterTestItemsByItemIdIn() {
    final int removed = clusterRepository.deleteClusterTestItemsByItemIds(List.of(1L));
    assertEquals(1, removed);

    final int zeroRemoved = clusterRepository.deleteClusterTestItemsByItemIds(
        Collections.emptyList());
    assertEquals(0, zeroRemoved);
  }

  @Test
  void shouldSaveClusterTestItems() {
    final Cluster cluster = clusterRepository.findAllByLaunchId(LAUNCH_ID).get(1);
    final int inserted = clusterRepository.saveClusterTestItems(cluster, Set.of(1L, 2L));
    assertEquals(2, inserted);
  }

  @Test
  void shouldFindByIndexAndLaunchId() {
    final Optional<Cluster> cluster = clusterRepository.findByIndexIdAndLaunchId(1L, 1L);
    assertTrue(cluster.isPresent());
  }

}
