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

package com.epam.ta.reportportal.model.log;

import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.item.PathNameResource;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchLogRs {

	@JsonProperty(value = "launchId")
	private Long launchId;

	@JsonProperty(value = "itemId")
	private Long itemId;

	@JsonProperty(value = "itemName")
	private String itemName;

	@JsonProperty(value = "path")
	private String path;

	@JsonProperty(value = "pathNames")
	private PathNameResource pathNames;

	@JsonProperty(value = "duration")
	private double duration;

	@JsonProperty(value = "status")
	private String status;

	@JsonProperty(value = "issue")
	private Issue issue;

	@JsonProperty(value = "patternTemplates")
	private Set<String> patternTemplates;

	@JsonProperty(value = "logs")
	private List<LogEntry> logs;

	public static class LogEntry {
		private String message;
		private String level;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getLevel() {
			return level;
		}

		public void setLevel(String level) {
			this.level = level;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("LogEntry{");
			sb.append("message='").append(message).append('\'');
			sb.append(", level='").append(level).append('\'');
			sb.append('}');
			return sb.toString();
		}
	}

	public Long getLaunchId() {
		return launchId;
	}

	public void setLaunchId(Long launchId) {
		this.launchId = launchId;
	}

	public PathNameResource getPathNames() {
		return pathNames;
	}

	public void setPathNames(PathNameResource pathNames) {
		this.pathNames = pathNames;
	}

	public Set<String> getPatternTemplates() {
		return patternTemplates;
	}

	public void setPatternTemplates(Set<String> patternTemplates) {
		this.patternTemplates = patternTemplates;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public List<LogEntry> getLogs() {
		return logs;
	}

	public void setLogs(List<LogEntry> logs) {
		this.logs = logs;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("SearchLogRs{");
		sb.append(", launchId=").append(launchId);
		sb.append(", itemId=").append(itemId);
		sb.append(", itemName='").append(itemName).append('\'');
		sb.append(", path='").append(path).append('\'');
		sb.append(", pathNames=").append(pathNames);
		sb.append(", duration=").append(duration);
		sb.append(", status='").append(status).append('\'');
		sb.append(", issue=").append(issue);
		sb.append(", patternTemplates=").append(patternTemplates);
		sb.append(", logs=").append(logs);
		sb.append('}');
		return sb.toString();
	}
}
