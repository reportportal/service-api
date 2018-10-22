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

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DemoDataRq {

	@JsonProperty(defaultValue = "10")
	private int launchesQuantity = 10;

	@JsonProperty(defaultValue = "false")
	private boolean isCreateDashboard = false;

	public boolean isCreateDashboard() {
		return isCreateDashboard;
	}

	public void setCreateDashboard(boolean createDashboard) {
		isCreateDashboard = createDashboard;
	}

	public int getLaunchesQuantity() {
		return launchesQuantity;
	}

	public void setLaunchesQuantity(int launchesQuantity) {
		this.launchesQuantity = launchesQuantity;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("DemoDataRq{");
		sb.append("launchesQuantity=").append(launchesQuantity);
		sb.append(", isCreateDashboard=").append(isCreateDashboard);
		sb.append('}');
		return sb.toString();
	}
}