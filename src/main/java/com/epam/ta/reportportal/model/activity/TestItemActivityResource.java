/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.model.activity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class TestItemActivityResource {

	@JsonProperty(value = "id", required = true)
	private Long id;

	@JsonProperty(value = "projectId", required = true)
	private Long projectId;

	@JsonProperty(value = "name", required = true)
	private String name;

	@JsonProperty(value = "issueDescription")
	private String issueDescription;

	@JsonProperty(value = "issueTypeLongName")
	private String issueTypeLongName;

	@JsonProperty(value = "ignoreAnalyzer")
	private boolean ignoreAnalyzer;

	@JsonProperty(value = "autoAnalyzed")
	private boolean autoAnalyzed;

	@JsonProperty(value = "tickets")
	private String tickets;

	@JsonProperty(value = "status")
	private String status;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIssueDescription() {
		return issueDescription;
	}

	public void setIssueDescription(String issueDescription) {
		this.issueDescription = issueDescription;
	}

	public String getIssueTypeLongName() {
		return issueTypeLongName;
	}

	public void setIssueTypeLongName(String issueTypeLongName) {
		this.issueTypeLongName = issueTypeLongName;
	}

	public boolean isIgnoreAnalyzer() {
		return ignoreAnalyzer;
	}

	public void setIgnoreAnalyzer(boolean ignoreAnalyzer) {
		this.ignoreAnalyzer = ignoreAnalyzer;
	}

	public boolean isAutoAnalyzed() {
		return autoAnalyzed;
	}

	public void setAutoAnalyzed(boolean autoAnalyzed) {
		this.autoAnalyzed = autoAnalyzed;
	}

	public String getTickets() {
		return tickets;
	}

	public void setTickets(String tickets) {
		this.tickets = tickets;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("TestItemActivityResource{");
		sb.append("id=").append(id);
		sb.append(", projectId=").append(projectId);
		sb.append(", name='").append(name).append('\'');
		sb.append(", issueDescription='").append(issueDescription).append('\'');
		sb.append(", issueTypeLongName='").append(issueTypeLongName).append('\'');
		sb.append(", ignoreAnalyzer=").append(ignoreAnalyzer);
		sb.append(", autoAnalyzed=").append(autoAnalyzed);
		sb.append(", tickets='").append(tickets).append('\'');
		sb.append(", status='").append(status).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
