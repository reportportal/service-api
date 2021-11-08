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

import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterData;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterInfoRs;
import com.epam.ta.reportportal.dao.ClusterRepository;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.cluster.Cluster;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.core.launch.cluster.CreateClusterHandlerImpl.RP_CLUSTER_LAST_RUN_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@ExtendWith(MockitoExtension.class)
class CreateClusterHandlerImplTest {

	@Mock
	private ClusterRepository clusterRepository;

	@Mock
	private LogRepository logRepository;

	@Mock
	private ItemAttributeRepository itemAttributeRepository;

	@InjectMocks
	private CreateClusterHandlerImpl createClusterHandler;

	@Test
	void createClusterWhenAttributeExists() {

		final ClusterData clusterData = new ClusterData();
		clusterData.setProject(1L);
		clusterData.setLaunchId(1L);

		final ClusterInfoRs first = new ClusterInfoRs();
		first.setClusterId(1L);
		first.setClusterMessage("first");
		first.setLogIds(List.of(1L, 2L));

		final ClusterInfoRs second = new ClusterInfoRs();
		second.setClusterId(2L);
		second.setClusterMessage("second");
		second.setLogIds(List.of(3L, 4L));

		clusterData.setClusters(List.of(first, second));

		doAnswer(invocation -> {
			Object[] args = invocation.getArguments();
			Cluster cluster = ((Cluster) args[0]);
			cluster.setId(cluster.getIndexId());
			return null;
		}).when(clusterRepository).save(any(Cluster.class));

		when(itemAttributeRepository.findByLaunchIdAndKeyAndSystem(clusterData.getLaunchId(), RP_CLUSTER_LAST_RUN_KEY, true)).thenReturn(
				Optional.of(new ItemAttribute()));

		createClusterHandler.create(clusterData);

		verify(clusterRepository, times(2)).save(any(Cluster.class));
		verify(logRepository, times(2)).updateClusterIdByIdIn(any(Long.class), anyList());
		verify(itemAttributeRepository, times(0)).saveByLaunchId(anyLong(), anyString(), anyString(), anyBoolean());
	}

	@Test
	void createClusterWhenAttributeNotExists() {

		final ClusterData clusterData = new ClusterData();
		clusterData.setProject(1L);
		clusterData.setLaunchId(1L);

		final ClusterInfoRs first = new ClusterInfoRs();
		first.setClusterId(1L);
		first.setClusterMessage("first");
		first.setLogIds(List.of(1L, 2L));

		final ClusterInfoRs second = new ClusterInfoRs();
		second.setClusterId(2L);
		second.setClusterMessage("second");
		second.setLogIds(List.of(3L, 4L));

		clusterData.setClusters(List.of(first, second));

		doAnswer(invocation -> {
			Object[] args = invocation.getArguments();
			Cluster cluster = ((Cluster) args[0]);
			cluster.setId(cluster.getIndexId());
			return null;
		}).when(clusterRepository).save(any(Cluster.class));

		when(itemAttributeRepository.findByLaunchIdAndKeyAndSystem(clusterData.getLaunchId(), RP_CLUSTER_LAST_RUN_KEY, true)).thenReturn(
				Optional.empty());

		createClusterHandler.create(clusterData);

		verify(clusterRepository, times(2)).save(any(Cluster.class));
		verify(logRepository, times(2)).updateClusterIdByIdIn(any(Long.class), anyList());
		verify(itemAttributeRepository, times(1)).saveByLaunchId(anyLong(), anyString(), anyString(), anyBoolean());
	}

}