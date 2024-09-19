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

package com.epam.ta.reportportal.model.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ProjectAttributesActivityResource {

	@JsonProperty(value = "projectId", required = true)
	private Long projectId;

	@JsonProperty(value = "projectName", required = true)
	private String projectName;

	@JsonProperty(value = "config")
	@JsonDeserialize(as = HashMap.class)
	private Map<String, String> config;

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Map<String, String> getConfig() {
		return config;
	}

	public void setConfig(Map<String, String> config) {
		this.config = config;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ProjectAttributesActivityResource{");
		sb.append("projectId=").append(projectId);
		sb.append(", projectName='").append(projectName).append('\'');
		sb.append(", config=").append(config);
		sb.append('}');
		return sb.toString();
	}
}
