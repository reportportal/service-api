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

package com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster;

import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ClusterInfoRs {

	private Long clusterId;
	private String clusterMessage;
	private List<Long> logIds;

	public ClusterInfoRs() {
	}

	public Long getClusterId() {
		return clusterId;
	}

	public void setClusterId(Long clusterId) {
		this.clusterId = clusterId;
	}

	public String getClusterMessage() {
		return clusterMessage;
	}

	public void setClusterMessage(String clusterMessage) {
		this.clusterMessage = clusterMessage;
	}

	public List<Long> getLogIds() {
		return logIds;
	}

	public void setLogIds(List<Long> logIds) {
		this.logIds = logIds;
	}
}
