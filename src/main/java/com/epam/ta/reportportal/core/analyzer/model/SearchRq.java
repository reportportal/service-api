/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.analyzer.model;

import java.util.List;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class SearchRq {

	private Long launchId;

	private String launchName;

	private Long itemId;

	private Long projectId;

	private List<Long> filteredLaunchIds;

	private List<String> logMessages;

	private SearchConfig searchConfig;

	public Long getLaunchId() {
		return launchId;
	}

	public void setLaunchId(Long launchId) {
		this.launchId = launchId;
	}

	public String getLaunchName() {
		return launchName;
	}

	public void setLaunchName(String launchName) {
		this.launchName = launchName;
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public List<Long> getFilteredLaunchIds() {
		return filteredLaunchIds;
	}

	public void setFilteredLaunchIds(List<Long> filteredLaunchIds) {
		this.filteredLaunchIds = filteredLaunchIds;
	}

	public List<String> getLogMessages() {
		return logMessages;
	}

	public void setLogMessages(List<String> logMessages) {
		this.logMessages = logMessages;
	}

	public SearchConfig getSearchConfig() {
		return searchConfig;
	}

	public void setSearchConfig(SearchConfig searchConfig) {
		this.searchConfig = searchConfig;
	}

	public static class SearchConfig {
		private String searchMode;
		private Integer numberOfLogLines;

		private SearchConfig(String searchMode, Integer numberOfLogLines) {
			this.searchMode = searchMode;
			this.numberOfLogLines = numberOfLogLines;
		}

		public String getSearchMode() {
			return searchMode;
		}

		public void setSearchMode(String searchMode) {
			this.searchMode = searchMode;
		}

		public Integer getNumberOfLogLines() {
			return numberOfLogLines;
		}

		public void setNumberOfLogLines(Integer numberOfLogLines) {
			this.numberOfLogLines = numberOfLogLines;
		}

		public static SearchConfig of(String searchMode, Integer numberOfLogLines) {
			return new SearchConfig(searchMode, numberOfLogLines);
		}
	}
}
