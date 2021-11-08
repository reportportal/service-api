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
import com.epam.ta.reportportal.dao.ClusterRepository;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.cluster.Cluster;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

import static com.epam.ta.reportportal.ws.converter.converters.ClusterConverter.TO_CLUSTER;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
@Transactional
public class CreateClusterHandlerImpl implements CreateClusterHandler {

	public static final String RP_CLUSTER_LAST_RUN_KEY = "rp.cluster.lastRun";

	private final ClusterRepository clusterRepository;
	private final LogRepository logRepository;
	private final ItemAttributeRepository itemAttributeRepository;

	@Autowired
	public CreateClusterHandlerImpl(ClusterRepository clusterRepository, LogRepository logRepository,
			ItemAttributeRepository itemAttributeRepository) {
		this.clusterRepository = clusterRepository;
		this.logRepository = logRepository;
		this.itemAttributeRepository = itemAttributeRepository;
	}

	@Override
	public void create(ClusterData clusterData) {
		ofNullable(clusterData.getClusters()).filter(CollectionUtils::isNotEmpty).ifPresent(clusters -> {
			clusters.stream().filter(c -> Objects.nonNull(c.getClusterId())).forEach(clusterInfoRs -> {
				final Cluster cluster = TO_CLUSTER.apply(clusterInfoRs);
				cluster.setProjectId(clusterData.getProject());
				cluster.setLaunchId(clusterData.getLaunchId());
				clusterRepository.save(cluster);
				logRepository.updateClusterIdByIdIn(cluster.getId(), clusterInfoRs.getLogIds());
			});
		});
		saveLastRunAttribute(clusterData);
	}

	private void saveLastRunAttribute(ClusterData clusterData) {
		final String lastRunDate = String.valueOf(Instant.now().toEpochMilli());
		itemAttributeRepository.findByLaunchIdAndKeyAndSystem(clusterData.getLaunchId(), RP_CLUSTER_LAST_RUN_KEY, true)
				.ifPresentOrElse(attr -> {
					attr.setValue(lastRunDate);
					itemAttributeRepository.save(attr);
				}, () -> itemAttributeRepository.saveByLaunchId(clusterData.getLaunchId(), RP_CLUSTER_LAST_RUN_KEY, lastRunDate, true));
	}

}
