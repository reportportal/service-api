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

import com.epam.ta.reportportal.ws.annotations.NotEmpty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DemoDataRq {

	@JsonProperty(defaultValue = "10")
	private int launchesQuantity = 10;

	@JsonProperty(defaultValue = "false")
	private boolean isCreateDashboard = false;

	@NotNull
	@NotEmpty
	@JsonProperty(required = true)
	@Size(min = 1, max = 90)
	private String postfix;

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

	public String getPostfix() {
		return postfix;
	}

	public void setPostfix(String postfix) {
		this.postfix = postfix;
	}

	@Override
	public String toString() {
		return "DemoDataRq{" + "launchesQuantity=" + launchesQuantity + ", isCreateDashboard=" + isCreateDashboard + ", postfix='" + postfix
				+ '\'' + '}';
	}
}
