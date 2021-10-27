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
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.cluster.Cluster;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.epam.ta.reportportal.ws.converter.converters.ClusterConverter.TO_CLUSTER;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
@Transactional
public class CreateClusterHandlerImpl implements CreateClusterHandler {

	private final ClusterRepository clusterRepository;
	private final LogRepository logRepository;

	@Autowired
	public CreateClusterHandlerImpl(ClusterRepository clusterRepository, LogRepository logRepository) {
		this.clusterRepository = clusterRepository;
		this.logRepository = logRepository;
	}

	@Override
	public void create(ClusterData clusterData) {
		ofNullable(clusterData.getClusterInfo()).filter(CollectionUtils::isNotEmpty).ifPresent(clusters -> {
			clusters.stream().filter(c -> Objects.nonNull(c.getClusterId())).forEach(clusterInfoRs -> {
				final Cluster cluster = TO_CLUSTER.apply(clusterInfoRs);
				clusterRepository.save(cluster);
				logRepository.updateClusterIdByIdIn(cluster.getId(), clusterInfoRs.getLogIds());
			});
		});
	}

}
