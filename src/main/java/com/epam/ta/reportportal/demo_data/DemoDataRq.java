/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.demo_data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
class DemoDataRq {

	@JsonProperty(defaultValue = "Demo Api Tests")
	private String launchName = "Demo Api Tests";

	@JsonProperty(defaultValue = "DEMO DASHBOARD")
	private String dashboardName = "DEMO DASHBOARD";

	@JsonProperty(defaultValue = "DEMO FILTER")
	private String filterName = "DEMO FILTER";

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

	public String getLaunchName() {
		return launchName;
	}

	public void setLaunchName(String launchName) {
		this.launchName = launchName;
	}

	public String getDashboardName() {
		return dashboardName;
	}

	public void setDashboardName(String dashboardName) {
		this.dashboardName = dashboardName;
	}

	public String getFilterName() {
		return filterName;
	}

	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}

	public int getLaunchesQuantity() {
		return launchesQuantity;
	}

	public void setLaunchesQuantity(int launchesQuantity) {
		this.launchesQuantity = launchesQuantity;
	}

	@Override
	public String toString() {
		return "DemoDataRq{" + "launchName='" + launchName + '\'' + ", dashboardName='" + dashboardName + '\'' + ", filterName='"
				+ filterName + '\'' + ", launchesQuantity=" + launchesQuantity + ", isCreateDashboard=" + isCreateDashboard + '}';
	}
}
