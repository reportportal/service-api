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

package com.epam.ta.reportportal.model.analyzer;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class CleanIndexRq {

	@JsonProperty("project")
	private Long projectId;

	@JsonProperty("ids")
	private List<Long> logIds;

	public CleanIndexRq() {
	}

	public CleanIndexRq(Long projectId, List<Long> logIds) {
		this.projectId = projectId;
		this.logIds = logIds;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public List<Long> getLogIds() {
		return logIds;
	}

	public void setLogIds(List<Long> logIds) {
		this.logIds = logIds;
	}
}
