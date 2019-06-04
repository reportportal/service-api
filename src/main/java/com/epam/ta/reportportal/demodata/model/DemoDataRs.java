/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.demodata.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Ihar Kahadouski
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DemoDataRs {

	@JsonProperty
	private Long dashboardId;

	@JsonProperty
	private List<Long> launchIds;

	public Long getDashboardId() {
		return dashboardId;
	}

	public void setDashboardId(Long dashboardId) {
		this.dashboardId = dashboardId;
	}

	public List<Long> getLaunchIds() {
		return launchIds;
	}

	public void setLaunchIds(List<Long> launchIds) {
		this.launchIds = launchIds;
	}
}
