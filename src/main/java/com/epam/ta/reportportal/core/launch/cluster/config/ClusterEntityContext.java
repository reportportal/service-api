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

package com.epam.ta.reportportal.core.launch.cluster.config;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ClusterEntityContext {

	private final Long launchId;
	private final Long projectId;

	private final List<Long> itemIds;

	private ClusterEntityContext(Long launchId, Long projectId) {
		this.launchId = launchId;
		this.projectId = projectId;
		this.itemIds = Collections.emptyList();
	}

	private ClusterEntityContext(Long launchId, Long projectId, List<Long> itemIds) {
		this.launchId = launchId;
		this.projectId = projectId;
		this.itemIds = itemIds;
	}

	public Long getLaunchId() {
		return launchId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public List<Long> getItemIds() {
		return itemIds;
	}

	public static ClusterEntityContext  of(Long launchId, Long projectId) {
		return new ClusterEntityContext(launchId, projectId);
	}

	public static ClusterEntityContext  of(Long launchId, Long projectId, List<Long> itemIds) {
		return new ClusterEntityContext(launchId, projectId, itemIds);
	}
}
