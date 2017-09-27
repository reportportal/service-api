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

package com.epam.ta.reportportal.util.analyzer.model;

import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents test item container in index/analysis request/response.
 *
 * @author Ivan Sharamet
 *
 */
public class IndexTestItem {

	@JsonProperty("testItemId")
	private String testItemId;
	@JsonProperty("issueType")
	private String issueType;
	@JsonProperty("logs")
	private List<IndexLog> logs;
	@JsonProperty("uniqueId")
	private String uniqueId;

	public static IndexTestItem fromTestItem(TestItem testItem, List<Log> logs) {
		IndexTestItem indexTestItem = new IndexTestItem();
		indexTestItem.setTestItemId(testItem.getId());
		indexTestItem.setUniqueId(testItem.getUniqueId());
		if (testItem.getIssue() != null) {
			indexTestItem.setIssueType(testItem.getIssue().getIssueType());
		}
		indexTestItem.setLogs(logs.stream().map(IndexLog::fromLog).collect(Collectors.toList()));
		return indexTestItem;
	}

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

	public List<IndexLog> getLogs() {
		return logs;
	}

	public void setLogs(List<IndexLog> logs) {
		this.logs = logs;
	}
}
