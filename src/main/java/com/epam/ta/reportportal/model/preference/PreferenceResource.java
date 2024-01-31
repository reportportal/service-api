/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.model.preference;

import com.epam.ta.reportportal.model.filter.UserFilterResource;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * JSON representation of report portal domain object
 *
 * @author Dzmitry_Kavalets
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreferenceResource {

	@JsonProperty(value = "userId")
	private Long userId;

	@JsonProperty(value = "projectId", required = true)
	private Long projectId;

	@JsonProperty(value = "filters")
	private List<UserFilterResource> filters;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public List<UserFilterResource> getFilters() {
		return filters;
	}

	public void setFilters(List<UserFilterResource> filters) {
		this.filters = filters;
	}

	@Override
	public String toString() {
		return "PreferenceResource{" + "userId=" + userId + ", projectId=" + projectId + ", filters=" + filters + '}';
	}
}
