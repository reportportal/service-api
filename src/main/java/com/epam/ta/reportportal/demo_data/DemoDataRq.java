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
