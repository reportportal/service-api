/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.demodata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Ihar Kahadouski
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class DemoDataRs {

	@JsonProperty
	private List<Long> dashboards;
	@JsonProperty
	private List<Long> launches;

	public List<Long> getDashboards() {
		return dashboards;
	}

	public void setDashboards(List<Long> dashboards) {
		this.dashboards = dashboards;
	}

	public List<Long> getLaunches() {
		return launches;
	}

	public void setLaunches(List<Long> launches) {
		this.launches = launches;
	}
}
