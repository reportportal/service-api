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

import java.util.Objects;
import java.util.Set;

/**
 * Represents test item container in index/analysis request/response.
 *
 * @author Ivan Sharamet
 */
public class IndexTestItem {

	@JsonProperty("testItemId")
	private String testItemId;

	@JsonProperty("issueType")
	private String issueType;

	@JsonProperty("logs")
	private Set<IndexLog> logs;

	@JsonProperty("uniqueId")
	private String uniqueId;

	// used for boost item if it was not analyzed by analyzer
	@JsonProperty("isAutoAnalyzed")
	private boolean isAutoAnalyzed;

	public IndexTestItem() {
	}

	public String getTestItemId() {
		return testItemId;
	}

	public void setTestItemId(String testItemId) {
		this.testItemId = testItemId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getIssueType() {
		return issueType;
	}

	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}

	public Set<IndexLog> getLogs() {
		return logs;
	}

	public void setLogs(Set<IndexLog> logs) {
		this.logs = logs;
	}

	public boolean isAutoAnalyzed() {
		return isAutoAnalyzed;
	}

	public void setAutoAnalyzed(boolean autoAnalyzed) {
		isAutoAnalyzed = autoAnalyzed;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		IndexTestItem that = (IndexTestItem) o;
		return isAutoAnalyzed == that.isAutoAnalyzed && Objects.equals(testItemId, that.testItemId) && Objects.equals(
				issueType, that.issueType) && Objects.equals(logs, that.logs) && Objects.equals(uniqueId, that.uniqueId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(testItemId, issueType, logs, uniqueId, isAutoAnalyzed);
	}
}
