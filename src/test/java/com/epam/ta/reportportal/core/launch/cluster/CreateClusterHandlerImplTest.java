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

package com.epam.ta.reportportal.core.launch.cluster;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterData;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterInfoRs;
import com.epam.ta.reportportal.dao.ClusterRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.cluster.Cluster;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@ExtendWith(MockitoExtension.class)
class CreateClusterHandlerImplTest {

  @Mock
  private ClusterRepository clusterRepository;

  @Mock
  private LogRepository logRepository;

  @InjectMocks
  private CreateClusterHandlerImpl createClusterHandler;

  @Test
  void updateCluster() {

    final ClusterData clusterData = new ClusterData();
    clusterData.setProject(1L);
    clusterData.setLaunchId(1L);

    final ClusterInfoRs first = new ClusterInfoRs();
    first.setClusterId(1L);
    first.setClusterMessage("first");
    first.setLogIds(Set.of(1L, 2L));
    first.setItemIds(Set.of(1L, 2L));

    final ClusterInfoRs second = new ClusterInfoRs();
    second.setClusterId(2L);
    second.setClusterMessage("second");
    second.setLogIds(Set.of(3L, 4L));
    second.setItemIds(Set.of(3L, 4L));

    clusterData.setClusters(List.of(first, second));

    final Cluster firstCluster = new Cluster();
    firstCluster.setIndexId(1L);
    final Cluster secondCluster = new Cluster();
    secondCluster.setIndexId(2L);
    when(clusterRepository.findByIndexIdAndLaunchId(1L, clusterData.getLaunchId())).thenReturn(
        Optional.of(firstCluster));
    when(clusterRepository.findByIndexIdAndLaunchId(2L, clusterData.getLaunchId())).thenReturn(
        Optional.of(secondCluster));

    doAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      Cluster cluster = ((Cluster) args[0]);
      cluster.setId(cluster.getIndexId());
      return cluster;
    }).when(clusterRepository).save(any(Cluster.class));

    createClusterHandler.create(clusterData);

    verify(clusterRepository, times(2)).save(any(Cluster.class));
    verify(clusterRepository, times(2)).saveClusterTestItems(any(Cluster.class), anySet());
    verify(logRepository, times(2)).updateClusterIdByIdIn(any(Long.class), anySet());
  }

  @Test
  void saveCluster() {

    final ClusterData clusterData = new ClusterData();
    clusterData.setProject(1L);
    clusterData.setLaunchId(1L);

    final ClusterInfoRs first = new ClusterInfoRs();
    first.setClusterId(1L);
    first.setClusterMessage("first");
    first.setLogIds(Set.of(1L, 2L));
    first.setItemIds(Set.of(1L, 2L));

    final ClusterInfoRs second = new ClusterInfoRs();
    second.setClusterId(2L);
    second.setClusterMessage("second");
    second.setLogIds(Set.of(3L, 4L));
    second.setItemIds(Set.of(3L, 4L));

    clusterData.setClusters(List.of(first, second));

    when(clusterRepository.findByIndexIdAndLaunchId(anyLong(),
        eq(clusterData.getLaunchId()))).thenReturn(Optional.empty());

    doAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      Cluster cluster = ((Cluster) args[0]);
      cluster.setId(cluster.getIndexId());
      return cluster;
    }).when(clusterRepository).save(any(Cluster.class));

    createClusterHandler.create(clusterData);

    verify(clusterRepository, times(2)).save(any(Cluster.class));
    verify(clusterRepository, times(2)).saveClusterTestItems(any(Cluster.class), anySet());
    verify(logRepository, times(2)).updateClusterIdByIdIn(any(Long.class), anySet());
  }

}