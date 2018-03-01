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

package com.epam.ta.reportportal.core.analyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Represents launch container in index/analysis request/response.
 *
 * @author Ivan Sharamet
 */
public class IndexLaunch {

	@JsonProperty("launchId")
	private String launchId;

	@JsonProperty("launchName")
	private String launchName;

	@JsonProperty("project")
	private String project;

	@JsonProperty("analyzeMode")
	private String analyzeMode;

	@JsonProperty("testItems")
	private List<IndexTestItem> testItems;

	public IndexLaunch() {
	}

	public String getAnalyzeMode() {
		return analyzeMode;
	}

	public void setAnalyzeMode(String analyzeMode) {
		this.analyzeMode = analyzeMode;
	}

	public String getLaunchId() {
		return launchId;
	}

	public void setLaunchId(String launchId) {
		this.launchId = launchId;
	}

	public String getLaunchName() {
		return launchName;
	}

	public void setLaunchName(String launchName) {
		this.launchName = launchName;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public List<IndexTestItem> getTestItems() {
		return testItems;
	}

	public void setTestItems(List<IndexTestItem> testItems) {
		this.testItems = testItems;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		IndexLaunch that = (IndexLaunch) o;
		return Objects.equals(launchId, that.launchId) && Objects.equals(launchName, that.launchName) && Objects.equals(
				project, that.project) && analyzeMode == that.analyzeMode && Objects.equals(testItems, that.testItems);
	}

	@Override
	public int hashCode() {
		return Objects.hash(launchId, launchName, project, analyzeMode, testItems);
	}
}
