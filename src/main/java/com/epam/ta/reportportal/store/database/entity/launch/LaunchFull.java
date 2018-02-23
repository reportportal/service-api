/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.store.database.entity.launch;

import java.util.Objects;

/**
 * @author Pavel Bortnik
 */
public class LaunchFull {

	private Launch launch;

	private ExecutionStatistics executionStatistics;

	public LaunchFull(Launch launch, ExecutionStatistics executionStatistics) {
		this.launch = launch;
		this.executionStatistics = executionStatistics;
	}

	public Launch getLaunch() {
		return launch;
	}

	public void setLaunch(Launch launch) {
		this.launch = launch;
	}

	public ExecutionStatistics getExecutionStatistics() {
		return executionStatistics;
	}

	public void setExecutionStatistics(ExecutionStatistics executionStatistics) {
		this.executionStatistics = executionStatistics;
	}

	@Override
	public String toString() {
		return "LaunchFull{" + "launch=" + launch + ", executionStatistics=" + executionStatistics + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		LaunchFull that = (LaunchFull) o;
		return Objects.equals(launch, that.launch) && Objects.equals(executionStatistics, that.executionStatistics);
	}

	@Override
	public int hashCode() {
		return Objects.hash(launch, executionStatistics);
	}
}
