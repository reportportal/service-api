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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterInfoRs;
import com.epam.ta.reportportal.entity.cluster.Cluster;
import com.epam.ta.reportportal.ws.model.launch.cluster.ClusterInfoResource;

import java.util.function.Function;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ClusterConverter {

	private ClusterConverter() {
		//static only
	}

	public static final Function<ClusterInfoRs, Cluster> TO_CLUSTER = rs -> {
		final Cluster cluster = new Cluster();
		cluster.setId(rs.getClusterId());
		cluster.setMessage(rs.getClusterMessage());
		return cluster;
	};

	public static final Function<Cluster, ClusterInfoResource> TO_CLUSTER_INFO = c -> {
		final ClusterInfoResource resource = new ClusterInfoResource();
		resource.setId(c.getId());
		resource.setLaunchId(c.getLaunchId());
		resource.setMessage(c.getMessage());
		return resource;
	};
}
