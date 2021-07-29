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
package com.epam.ta.reportportal.core.analyzer.auto.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.Objects;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class IndexLaunchRemove {

	@JsonProperty("project")
	private Long projectId;

	@JsonProperty("launch_ids")
	private Collection<Long> launchIds;

	public IndexLaunchRemove(Long projectId, Collection<Long> launchIds) {
		this.projectId = projectId;
		this.launchIds = launchIds;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Collection<Long> getLaunchIds() {
		return launchIds;
	}

	public void setLaunchIds(Collection<Long> launchIds) {
		this.launchIds = launchIds;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		IndexLaunchRemove that = (IndexLaunchRemove) o;
		return Objects.equals(projectId, that.projectId) && Objects.equals(launchIds, that.launchIds);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, launchIds);
	}
}
